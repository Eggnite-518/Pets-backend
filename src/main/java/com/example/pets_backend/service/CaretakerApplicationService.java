package com.example.pets_backend.service;

import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.PetArchiveDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dto.resp.ActiveOrderRespDTO;
import com.example.pets_backend.dto.resp.MyApplicationRespDTO;
import com.example.pets_backend.enums.OrderStatusEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.example.pets_backend.service.support.OrderAddressFormatSupport;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaretakerApplicationService {

    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int ORDER_STATUS_BOUNTY = 1;
    private static final int ORDER_STATUS_PENDING_FULFILLMENT = 3;
    private static final int ORDER_STATUS_IN_FULFILLMENT = 4;
    private static final int ORDER_STATUS_PENDING_CONFIRMATION = 5;
    private static final int ORDER_STATUS_COMPLETED = 6;
    private static final int APPLY_STATUS_APPLYING = 0;
    private static final String APPLYING_STATUS_TEXT = "\u7b49\u5f85\u53cd\u9988";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final OrderApplicationDao orderApplicationDao;
    private final OrderDao orderDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final OrderAddressSnapshotDao orderAddressSnapshotDao;
    private final PetArchiveDao petArchiveDao;
    private final SitterProfileDao sitterProfileDao;
    private final OssAccessibleUrlService ossAccessibleUrlService;
    private final ProviderProfileSupportService providerProfileSupportService;

    /**
     * 宠托师“正在报名中”列表：当前用户已报名且订单仍为悬赏中。
     */
    public List<MyApplicationRespDTO> listMyApplications() {
        Long providerId = currentUserId();
        requireCaretakerRole();

        List<OrderApplicationDO> applications = orderApplicationDao.selectApplyingByProviderId(providerId);
        if (applications.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = applications.stream()
                .map(OrderApplicationDO::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, OrderDO> orders = orderDao.selectByIds(orderIds).stream()
                .filter(order -> Objects.equals(order.getStatus(), ORDER_STATUS_BOUNTY))
                .collect(Collectors.toMap(OrderDO::getOrderId, order -> order, (a, b) -> a));
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<Long, List<OrderPetSnapshotDO>> snapshotsByOrderId = orderPetSnapshotDao.selectByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderPetSnapshotDO::getOrderId));
        Map<Long, PetArchiveDO> petArchives = loadPetArchiveMap(snapshotsByOrderId);
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);

        List<MyApplicationRespDTO> result = new ArrayList<>();
        for (OrderApplicationDO application : applications) {
            OrderDO order = orders.get(application.getOrderId());
            if (order == null) {
                continue;
            }

            List<OrderPetSnapshotDO> snapshots = snapshotsByOrderId.getOrDefault(order.getOrderId(), List.of());
            OrderPetSnapshotDO firstPet = snapshots.isEmpty() ? null : snapshots.get(0);
            PetArchiveDO firstPetArchive = firstPet == null ? null : petArchives.get(firstPet.getArchivePetId());
            OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
            Double distanceKm = providerProfileSupportService.resolveDistanceKm(addressSnapshot, profile);
            Integer serviceType = resolveServiceType(order, firstPet);

            result.add(new MyApplicationRespDTO(
                    application.getApplyId() == null ? null : application.getApplyId().toString(),
                    order.getOrderId() == null ? null : order.getOrderId().toString(),
                    order.getStatus(),
                    resolveApplicationStatusText(order.getStatus()),
                    serviceType,
                    resolveServiceTypeText(serviceType),
                    toIntegerAmount(order.getTotalAmount()),
                    distanceKm,
                    order.getServiceDate() == null ? "" : order.getServiceDate().toString(),
                    formatTimeSlot(order.getServiceStartTime(), order.getServiceEndTime()),
                    formatAddressSnapshot(addressSnapshot),
                    buildPetNameSummary(snapshots),
                    firstPetArchive == null ? ""
                            : nullToEmpty(ossAccessibleUrlService.toDisplayUrl(firstPetArchive.getImage()))));
        }
        return result;
    }

    /**
     * 宠托师首页“正在履约中”订单列表，仅返回待履约/履约中订单。
     */
    public List<ActiveOrderRespDTO> listMyActiveOrders() {
        requireCaretakerRole();
        return listOrdersByStatuses(
                List.of(ORDER_STATUS_PENDING_FULFILLMENT, ORDER_STATUS_IN_FULFILLMENT),
                Comparator
                        .comparing((OrderDO order) -> !Objects.equals(order.getStatus(), ORDER_STATUS_IN_FULFILLMENT))
                        .thenComparing(OrderDO::getServiceDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OrderDO::getServiceStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OrderDO::getOrderId, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * 宠托师首页“等待确认”订单列表：打卡已完成，等待宠主确认结算。
     */
    public List<ActiveOrderRespDTO> listPendingConfirmationOrders() {
        requireCaretakerRole();
        return listOrdersByStatuses(
                List.of(ORDER_STATUS_PENDING_CONFIRMATION),
                Comparator
                        .comparing(OrderDO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OrderDO::getOrderId, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    /**
     * 宠托师今日已完成订单：服务日期为今天且状态为已完成。
     */
    public List<ActiveOrderRespDTO> listTodayCompletedOrders() {
        requireCaretakerRole();
        Long providerId = currentUserId();
        List<OrderDO> orders = orderDao.selectByProviderIdStatusAndServiceDate(
                providerId, ORDER_STATUS_COMPLETED, LocalDate.now());
        if (orders.isEmpty()) {
            return List.of();
        }
        return listOrdersFromEntities(orders, Comparator
                .comparing(OrderDO::getServiceStartTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(OrderDO::getOrderId, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private List<ActiveOrderRespDTO> listOrdersByStatuses(List<Integer> statuses, Comparator<OrderDO> comparator) {
        Long providerId = currentUserId();
        List<OrderDO> orders = orderDao.selectByProviderIdAndStatuses(providerId, statuses);
        if (orders.isEmpty()) {
            return List.of();
        }
        return listOrdersFromEntities(orders, comparator);
    }

    private List<ActiveOrderRespDTO> listOrdersFromEntities(List<OrderDO> orders, Comparator<OrderDO> comparator) {
        List<Long> orderIds = orders.stream()
                .map(OrderDO::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, List<OrderPetSnapshotDO>> snapshotsByOrderId = orderPetSnapshotDao.selectByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderPetSnapshotDO::getOrderId));
        Map<Long, OrderAddressSnapshotDO> addressSnapshots = orderAddressSnapshotDao.selectByIds(orders.stream()
                        .map(OrderDO::getAddressSnapshotId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(OrderAddressSnapshotDO::getSnapshotId, snapshot -> snapshot, (a, b) -> a));

        return orders.stream()
                .sorted(comparator)
                .map(order -> buildActiveOrder(order,
                        snapshotsByOrderId.getOrDefault(order.getOrderId(), List.of()),
                        addressSnapshots.get(order.getAddressSnapshotId())))
                .toList();
    }

    public boolean hasApplied(Long providerId, Long orderId) {
        if (providerId == null || orderId == null) {
            return false;
        }
        OrderApplicationDO application = orderApplicationDao.selectByOrderIdAndProviderId(orderId, providerId);
        return application != null && Objects.equals(application.getApplyStatus(), APPLY_STATUS_APPLYING);
    }

    private ActiveOrderRespDTO buildActiveOrder(OrderDO order, List<OrderPetSnapshotDO> petSnapshots,
            OrderAddressSnapshotDO addressSnapshot) {
        OrderPetSnapshotDO firstPet = petSnapshots.isEmpty() ? null : petSnapshots.get(0);
        return new ActiveOrderRespDTO(
                order.getOrderId() == null ? null : order.getOrderId().toString(),
                order.getStatus(),
                activeOrderStatusText(order.getStatus()),
                resolveServiceTypeText(resolveServiceType(order, firstPet)),
                order.getServiceDate() == null ? "" : order.getServiceDate().toString(),
                formatTimeSlot(order.getServiceStartTime(), order.getServiceEndTime()),
                formatAddressSnapshot(addressSnapshot),
                firstPet == null ? "" : nullToEmpty(firstPet.getSnapPetName()),
                "");
    }

    private int toIntegerAmount(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.setScale(0, java.math.RoundingMode.HALF_UP).intValue();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String activeOrderStatusText(Integer status) {
        return switch (status) {
            case ORDER_STATUS_PENDING_FULFILLMENT -> "\u5f85\u4e0a\u95e8";
            case ORDER_STATUS_IN_FULFILLMENT -> "\u5c65\u7ea6\u4e2d";
            case ORDER_STATUS_PENDING_CONFIRMATION -> "\u5f85\u5ba0\u4e3b\u786e\u8ba4";
            default -> nullToEmpty(OrderStatusEnum.getDescByCode(status));
        };
    }

    private Integer resolveServiceType(OrderDO order, OrderPetSnapshotDO firstPet) {
        if (order != null && order.getServiceType() != null) {
            return order.getServiceType();
        }
        return firstPet == null ? null : firstPet.getSnapPetType();
    }

    private String resolveServiceTypeText(Integer serviceType) {
        return switch (serviceType == null ? -1 : serviceType) {
            case 1 -> "\u4e0a\u95e8\u5582\u732b";
            case 2 -> "\u4e0a\u95e8\u905b\u72d7";
            default -> "";
        };
    }

    private String resolveApplicationStatusText(Integer status) {
        if (Objects.equals(status, ORDER_STATUS_BOUNTY)) {
            return APPLYING_STATUS_TEXT;
        }
        return nullToEmpty(OrderStatusEnum.getDescByCode(status));
    }

    private String formatTimeSlot(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return "";
        }
        return TIME_FORMATTER.format(startTime) + "-" + TIME_FORMATTER.format(endTime);
    }

    private String formatAddressSnapshot(OrderAddressSnapshotDO snapshot) {
        return OrderAddressFormatSupport.formatFullAddress(snapshot);
    }

    private String buildPetNameSummary(List<OrderPetSnapshotDO> snapshots) {
        return snapshots.stream()
                .map(OrderPetSnapshotDO::getSnapPetName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.joining("\u3001"));
    }

    private Map<Long, PetArchiveDO> loadPetArchiveMap(Map<Long, List<OrderPetSnapshotDO>> snapshotsByOrderId) {
        List<Long> petIds = snapshotsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(OrderPetSnapshotDO::getArchivePetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return petArchiveDao.selectByIds(petIds).stream()
                .collect(Collectors.toMap(PetArchiveDO::getPetId, pet -> pet, (a, b) -> a));
    }

    private void requireCaretakerRole() {
        Integer roleType = UserContext.getRoleType();
        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR);
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}
