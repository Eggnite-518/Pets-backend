package com.example.pets_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.PetArchiveDao;
import com.example.pets_backend.dao.UserAddressDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.CreateOrderReqDTO;
import com.example.pets_backend.dto.req.OrderServiceFeeQuoteReqDTO;
import com.example.pets_backend.dto.resp.ApplicationBriefRespDTO;
import com.example.pets_backend.dto.resp.CreateOrderRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordsRespDTO;
import com.example.pets_backend.dto.resp.MyRewardingOrderRespDTO;
import com.example.pets_backend.dto.resp.OpenOrderPageRespDTO;
import com.example.pets_backend.dto.resp.OpenOrderPetDTO;
import com.example.pets_backend.dto.resp.OpenOrderRespDTO;
import com.example.pets_backend.dto.resp.OrderDetailRespDTO;
import com.example.pets_backend.dto.resp.OrderPetBriefRespDTO;
import com.example.pets_backend.dto.resp.OrderServiceFeeQuoteRespDTO;
import com.example.pets_backend.dto.resp.ProviderDetailRespDTO;
import com.example.pets_backend.dto.resp.ReorderPrefillRespDTO;
import com.example.pets_backend.dto.resp.ServiceItemRespDTO;
import com.example.pets_backend.enums.OrderApplicationStatusEnum;
import com.example.pets_backend.enums.OrderHardFilterTagEnum;
import com.example.pets_backend.enums.OrderStatusEnum;
import com.example.pets_backend.enums.PetTypeEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.fulfillment.FulfillmentNodeType;
import com.example.pets_backend.service.fulfillment.ServiceFulfillmentFlow;
import com.example.pets_backend.service.support.FulfillmentCompletionSupport;
import com.example.pets_backend.service.support.FulfillmentRecordQuerySupport;
import com.example.pets_backend.service.support.OrderAddressFormatSupport;
import com.example.pets_backend.service.support.OrderHardFilterService;
import com.example.pets_backend.service.support.OrderRequirementTagService;
import com.example.pets_backend.service.support.OrderServiceFeeCalculator;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter FULFILLMENT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;

    private static final int ORDER_STATUS_BOUNTY = 1;
    private static final int ORDER_STATUS_PENDING_PAY = 2;
    private static final int ORDER_STATUS_PENDING_FULFILL = 3;

    private static final int APPLY_STATUS_REJECTED = 1;
    private static final int APPLY_STATUS_SELECTED = 2;

    private static final int PET_TYPE_CAT = 1;
    private static final int PET_TYPE_DOG = 2;
    private static final int PET_TYPE_EXOTIC = 3;

    private static final int SERVICE_TYPE_FEED = 1;
    private static final int SERVICE_TYPE_WALK = 2;
    private static final String SERVICE_ACTION_TEXT_FEED = "喂";
    private static final String SERVICE_ACTION_TEXT_WALK = "遛";

    private static final String STATUS_TEXT_BOUNTY = "等待反馈";

    private static final int PAGE_DEFAULT = 1;
    private static final int PAGE_SIZE_DEFAULT = 10;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final OrderDao orderDao;
    private final OrderApplicationDao orderApplicationDao;
    private final OrderAddressSnapshotDao orderAddressSnapshotDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final PetArchiveDao petArchiveDao;
    private final UserAddressDao userAddressDao;
    private final UserDao userDao;
    private final OrderSettlementService orderSettlementService;
    private final OrderCandidateService orderCandidateService;
    private final OrderHardFilterService orderHardFilterService;
    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final FulfillmentMediaStorageService fulfillmentMediaStorageService;
    private final OrderRequirementTagService orderRequirementTagService;
    private final OrderServiceFeeCalculator orderServiceFeeCalculator;
    private final OrderBountyPushService orderBountyPushService;
    private final SitterProfileDao sitterProfileDao;
    private final ProviderProfileSupportService providerProfileSupportService;
    private final OssAccessibleUrlService ossAccessibleUrlService;

    @Transactional
    public CreateOrderRespDTO create(CreateOrderReqDTO reqDTO) {
        validateCreateOrderRequest(reqDTO);
        Long ownerId = currentUserId();
        UserAddressDO address = requireOwnedAddress(reqDTO.addressId(), ownerId);
        List<PetArchiveDO> pets = requireOwnedPets(reqDTO.petIds(), ownerId);

        OrderAddressSnapshotDO addressSnapshot = buildAddressSnapshot(address);
        orderAddressSnapshotDao.insert(addressSnapshot);

        OrderDO order = buildOrder(reqDTO, ownerId, addressSnapshot.getSnapshotId());
        orderDao.insert(order);
        pets.forEach(pet -> orderPetSnapshotDao.insert(buildPetSnapshot(order.getOrderId(), pet, reqDTO.remark())));

        orderBountyPushService.notifyEligibleProviders(order);

        List<String> hardFilterTags = orderHardFilterService.parseTags(order);
        return new CreateOrderRespDTO(order.getOrderId(), order.getTotalAmount(), order.getStatus(),
                OrderStatusEnum.getDescByCode(order.getStatus()),
                order.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(order.getCreatedAt()),
                hardFilterTags,
                OrderHardFilterTagEnum.describeTags(hardFilterTags),
                orderRequirementTagService.toRespDTO(order.getRequirementTagsJson()));
    }

    public OrderServiceFeeQuoteRespDTO quote(OrderServiceFeeQuoteReqDTO reqDTO) {
        validateQuoteRequest(reqDTO);
        Long ownerId = currentUserId();
        UserAddressDO address = requireOwnedAddress(reqDTO.addressId(), ownerId);
        List<PetArchiveDO> pets = requireOwnedPets(reqDTO.petIds(), ownerId);
        return orderServiceFeeCalculator.quote(address, reqDTO.serviceDate(), pets, reqDTO.requirementTags());
    }

    public OpenOrderPageRespDTO listOpenOrders(Integer petType, Integer serviceType, Integer page, Integer pageSize) {
        int pageNum = normalizePage(page);
        int size = normalizePageSize(pageSize);
        validateOpenOrderFilters(petType, serviceType);

        IPage<OrderDO> orderPage = petType == null && serviceType == null
                ? orderDao.selectOpenOrderPage(pageNum, size)
                : orderDao.selectOpenOrderPageByFilters(pageNum, size, petType, serviceType);
        List<OrderDO> orders = orderPage.getRecords();
        if (orders == null || orders.isEmpty()) {
            return new OpenOrderPageRespDTO(orderPage.getTotal(), pageNum, size, List.of());
        }

        List<Long> orderIds = orders.stream().map(OrderDO::getOrderId).toList();
        Map<Long, List<OrderPetSnapshotDO>> snapshotsByOrderId = loadSnapshotsByOrderId(orderIds);
        Map<Long, Long> applicationCounts = loadApplicationCounts(orderIds);
        Map<Long, OrderAddressSnapshotDO> addressSnapshots = loadAddressSnapshots(orders);
        SitterProfileDO viewerProfile = resolveCaretakerProfileIfPresent();
        Set<Long> appliedOrderIds = loadAppliedOrderIds(viewerProfile);

        List<Long> ownerIds = orders.stream().map(OrderDO::getOwnerId).distinct().toList();
        Map<Long, UserDO> ownerMap = userDao.selectByIds(ownerIds).stream()
                .collect(Collectors.toMap(UserDO::getUserId, Function.identity()));

        List<OpenOrderRespDTO> list = orders.stream()
                .filter(order -> viewerProfile == null || orderHardFilterService.isProviderEligible(
                        viewerProfile, addressSnapshots.get(order.getAddressSnapshotId()), order))
                .map(order -> toOpenOrderResp(order, snapshotsByOrderId.getOrDefault(order.getOrderId(), List.of()),
                        applicationCounts.getOrDefault(order.getOrderId(), 0L),
                        addressSnapshots.get(order.getAddressSnapshotId()),
                        viewerProfile,
                        ownerMap.get(order.getOwnerId()),
                        appliedOrderIds.contains(order.getOrderId())))
                .toList();

        return new OpenOrderPageRespDTO(list.size(), pageNum, size, list);
    }

    public List<MyRewardingOrderRespDTO> listMyRewarding() {
        return listMyOrdersByStatus(ORDER_STATUS_BOUNTY);
    }

    public List<MyRewardingOrderRespDTO> listMyOrders(Integer status) {
        Long userId = currentUserId();
        List<OrderDO> orders = status == null
                ? orderDao.selectByOwnerId(userId)
                : orderDao.selectByOwnerIdAndStatus(userId, status);
        return buildMyOrderResponses(orders);
    }

    public List<MyRewardingOrderRespDTO> listMyOpen() {
        return listMyOrdersByStatus(ORDER_STATUS_BOUNTY);
    }

    public OrderDetailRespDTO getOrderDetail(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }

        List<OrderPetSnapshotDO> snapshots = orderPetSnapshotDao.selectByOrderIds(List.of(orderId));
        List<OrderDetailRespDTO.PetBrief> pets = snapshots.stream()
                .map(snapshot -> new OrderDetailRespDTO.PetBrief(
                        snapshot.getArchivePetId() == null ? null : snapshot.getArchivePetId().toString(),
                        snapshot.getSnapPetName(),
                        snapshot.getSnapPetType()))
                .toList();

        List<ApplicationBriefRespDTO> applicationBriefs = loadApplicationBriefs(List.of(orderId))
                .getOrDefault(orderId, List.of());
        List<OrderDetailRespDTO.ApplicationBrief> applications = applicationBriefs.stream()
                .map(application -> new OrderDetailRespDTO.ApplicationBrief(
                        application.applicationId() == null ? null : application.applicationId().toString(),
                        application.providerId() == null ? null : application.providerId().toString(),
                        application.providerNickname(),
                        application.providerAvatarUrl(),
                        application.applyStatus()))
                .toList();

        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        List<String> hardFilterTags = orderHardFilterService.parseTags(order);
        String remark = resolveRemarkFromSnapshots(snapshots);

        return new OrderDetailRespDTO(
                order.getOrderId() == null ? null : order.getOrderId().toString(),
                order.getServiceDate() == null ? null : order.getServiceDate().toString(),
                formatAmount(order.getTotalAmount()),
                formatAddress(addressSnapshot),
                order.getStatus(),
                serviceTypeToServiceText(order.getServiceType()),
                hardFilterTags,
                OrderHardFilterTagEnum.describeTags(hardFilterTags),
                orderRequirementTagService.toDetailRespDTO(order.getRequirementTagsJson()),
                remark,
                pets,
                applications);
    }

    public FulfillmentRecordsRespDTO listFulfillmentRecords(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        return buildFulfillmentRecords(order);
    }

    private FulfillmentRecordsRespDTO buildFulfillmentRecords(OrderDO order) {
        List<Integer> checklistNodeTypes = ServiceFulfillmentFlow
                .fromServiceType(order.getServiceType())
                .nodeCodes();
        List<FulfillmentRecordRespDTO> records = FulfillmentRecordQuerySupport
                .pickLatestDisplayRecords(fulfillmentRecordDao.selectByOrderId(order.getOrderId()))
                .stream()
                .map(record -> new FulfillmentRecordRespDTO(
                        record.getNodeType(),
                        FulfillmentNodeType.getDescByCode(record.getNodeType()),
                        fulfillmentMediaStorageService.resolveAccessibleUrl(record),
                        record.getMediaType(),
                        record.getObjectKey(),
                        record.getFileSize(),
                        record.getContentType(),
                        record.getFrameRate(),
                        record.getProcessingStatus(),
                        record.getProcessingErrorCode(),
                        record.getProcessingError(),
                        record.getWatermarkText(),
                        record.getCreatedAt() == null
                                ? null
                                : FULFILLMENT_TIME_FORMATTER.format(record.getCreatedAt())))
                .toList();
        boolean allNodesCompleted = FulfillmentCompletionSupport
                .areAllChecklistNodesUploaded(order, fulfillmentRecordDao);
        return new FulfillmentRecordsRespDTO(checklistNodeTypes, records, allNodesCompleted);
    }

    /**
     * 再来一单预填：复制除服务时间外的历史参数，供前端打开发单页预填。
     */
    public ReorderPrefillRespDTO reorderPrefill(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }

        List<OrderPetSnapshotDO> snapshots = orderPetSnapshotDao.selectByOrderIds(List.of(orderId));
        List<Long> petIds = snapshots.stream()
                .map(OrderPetSnapshotDO::getArchivePetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        UserAddressDO address = null;
        if (addressSnapshot != null && addressSnapshot.getSourceAddressId() != null) {
            address = userAddressDao.selectByAddressIdAndUserId(addressSnapshot.getSourceAddressId(), ownerId);
        }

        List<String> hardFilterTags = orderHardFilterService.parseTags(order);
        String remark = resolveRemarkFromSnapshots(snapshots);

        return new ReorderPrefillRespDTO(
                order.getOrderId() == null ? null : order.getOrderId().toString(),
                address == null ? null : address.getAddressId(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getContactName())
                        : address.getContactName(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getContactPhone())
                        : address.getContactPhone(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getProvince())
                        : address.getProvince(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getCity())
                        : address.getCity(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getDistrict())
                        : address.getDistrict(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getDetailAddress())
                        : address.getDetailAddress(),
                address == null ? nullToEmpty(addressSnapshot == null ? null : addressSnapshot.getAddressTag())
                        : address.getAddressTag(),
                order.getServiceType(),
                petIds,
                hardFilterTags,
                OrderHardFilterTagEnum.describeTags(hardFilterTags),
                orderRequirementTagService.toRespDTO(order.getRequirementTagsJson()),
                remark);
    }

    private String resolveRemarkFromSnapshots(List<OrderPetSnapshotDO> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return null;
        }
        return snapshots.stream()
                .map(OrderPetSnapshotDO::getSnapReq)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(note -> !note.isEmpty())
                .findFirst()
                .orElse(null);
    }

    public ProviderDetailRespDTO getProviderDetail(Long orderId, Long providerId) {
        return orderCandidateService.getProviderDetail(orderId, providerId);
    }

    @Transactional
    public void selectProvider(Long orderId, Long providerId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_BOUNTY)) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OPEN_ERROR);
        }

        OrderApplicationDO application = orderApplicationDao.selectByOrderIdAndProviderId(orderId, providerId);
        if (application == null) {
            throw new ClientException(BaseErrorCode.ORDER_APPLICATION_NOT_FOUND_ERROR);
        }

        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        orderHardFilterService.ensureProviderEligible(profile, addressSnapshot, order);

        orderDao.updateProviderIdAndStatus(orderId, providerId, ORDER_STATUS_PENDING_PAY);
        orderApplicationDao.updateApplyStatus(orderId, providerId, APPLY_STATUS_SELECTED);
        orderApplicationDao.updateApplyStatusForOthers(orderId, providerId, APPLY_STATUS_REJECTED);
    }

    @Transactional
    public void payOrder(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new ClientException(BaseErrorCode.ORDER_PAYMENT_ERROR);
        }

        orderSettlementService.markPaidAndCreateEscrow(orderId);
    }

    private List<MyRewardingOrderRespDTO> listMyOrdersByStatus(Integer status) {
        Long ownerId = currentUserId();
        List<OrderDO> orders = orderDao.selectByOwnerIdAndStatus(ownerId, status);
        return buildMyOrderResponses(orders);
    }

    private List<MyRewardingOrderRespDTO> buildMyOrderResponses(List<OrderDO> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(OrderDO::getOrderId).toList();
        Map<Long, List<OrderPetSnapshotDO>> snapshotsByOrderId = loadSnapshotsByOrderId(orderIds);
        Map<Long, List<ApplicationBriefRespDTO>> applicationsByOrderId = loadApplicationBriefs(orderIds);
        Map<Long, OrderAddressSnapshotDO> addressSnapshots = loadAddressSnapshots(orders);

        return orders.stream()
                .map(order -> {
                    List<String> hardFilterTags = orderHardFilterService.parseTags(order);
                    return new MyRewardingOrderRespDTO(
                            order.getOrderId(),
                            order.getServiceDate(),
                            order.getTotalAmount(),
                            formatAddress(addressSnapshots.get(order.getAddressSnapshotId())),
                            order.getStatus(),
                            OrderStatusEnum.getDescByCode(order.getStatus()),
                            hardFilterTags,
                            OrderHardFilterTagEnum.describeTags(hardFilterTags),
                            buildOrderPetBriefs(snapshotsByOrderId.getOrDefault(order.getOrderId(), List.of())),
                            applicationsByOrderId.getOrDefault(order.getOrderId(), List.of()));
                })
                .toList();
    }

    private Map<Long, List<OrderPetSnapshotDO>> loadSnapshotsByOrderId(List<Long> orderIds) {
        return orderPetSnapshotDao.selectByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderPetSnapshotDO::getOrderId));
    }

    private Map<Long, Long> loadApplicationCounts(List<Long> orderIds) {
        List<OrderApplicationDO> applications = orderApplicationDao.selectByOrderIds(orderIds);
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyMap();
        }
        return applications.stream()
                .collect(Collectors.groupingBy(OrderApplicationDO::getOrderId, Collectors.counting()));
    }

    private Map<Long, List<ApplicationBriefRespDTO>> loadApplicationBriefs(List<Long> orderIds) {
        List<OrderApplicationDO> applications = orderApplicationDao.selectByOrderIds(orderIds);
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> providerIds = applications.stream()
                .map(OrderApplicationDO::getProviderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserDO> userMap = userDao.selectByIds(providerIds).stream()
                .collect(Collectors.toMap(UserDO::getUserId, Function.identity()));

        return applications.stream()
                .collect(Collectors.groupingBy(
                        OrderApplicationDO::getOrderId,
                        Collectors.mapping(application -> {
                            UserDO user = userMap.get(application.getProviderId());
                            return new ApplicationBriefRespDTO(
                                    application.getApplyId(),
                                    application.getProviderId(),
                                    user == null ? null : user.getNickname(),
                                    user == null ? null : user.getAvatarUrl(),
                                    application.getApplyStatus(),
                                    OrderApplicationStatusEnum.getDescByCode(application.getApplyStatus()));
                        }, Collectors.toList())));
    }

    private Map<Long, OrderAddressSnapshotDO> loadAddressSnapshots(List<OrderDO> orders) {
        List<Long> snapshotIds = orders.stream()
                .map(OrderDO::getAddressSnapshotId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return orderAddressSnapshotDao.selectByIds(snapshotIds).stream()
                .collect(Collectors.toMap(OrderAddressSnapshotDO::getSnapshotId, Function.identity()));
    }

    private OpenOrderRespDTO toOpenOrderResp(OrderDO order, List<OrderPetSnapshotDO> snapshots,
            long applicationCount, OrderAddressSnapshotDO addressSnapshot, SitterProfileDO viewerProfile,
            UserDO owner, boolean hasApplied) {
        List<String> hardFilterTags = orderHardFilterService.parseTags(order);
        Double distanceKm = viewerProfile == null
                ? null
                : providerProfileSupportService.resolveDistanceKm(addressSnapshot, viewerProfile);
        return new OpenOrderRespDTO(
                order.getOrderId(),
                buildServiceItems(snapshots, order.getServiceType()),
                toIntegerAmount(order.getTotalAmount()),
                order.getServiceDate() == null ? null : order.getServiceDate().toString(),
                formatServiceTimeSlot(order.getServiceStartTime(), order.getServiceEndTime()),
                OrderAddressFormatSupport.formatCityDistrict(addressSnapshot),
                distanceKm,
                (int) applicationCount,
                false,
                hasApplied,
                snapshots.stream()
                        .map(snapshot -> new OpenOrderPetDTO(
                                snapshot.getSnapPetName(),
                                snapshot.getSnapPetType(),
                                PetTypeEnum.getDescByCode(snapshot.getSnapPetType())))
                        .toList(),
                order.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(order.getCreatedAt()),
                hardFilterTags,
                OrderHardFilterTagEnum.describeTags(hardFilterTags),
                owner == null ? null : owner.getNickname(),
                owner == null ? null : ossAccessibleUrlService.toDisplayUrl(owner.getAvatarUrl()));
    }

    private List<OrderPetBriefRespDTO> buildOrderPetBriefs(List<OrderPetSnapshotDO> snapshots) {
        return snapshots.stream()
                .map(snapshot -> new OrderPetBriefRespDTO(
                        snapshot.getArchivePetId(),
                        snapshot.getSnapPetName(),
                        snapshot.getSnapPetType(),
                        PetTypeEnum.getDescByCode(snapshot.getSnapPetType())))
                .toList();
    }

    private List<ServiceItemRespDTO> buildServiceItems(List<OrderPetSnapshotDO> snapshots, Integer serviceType) {
        return snapshots.stream()
                .map(snapshot -> {
                    String serviceText = buildServiceTypeText(serviceType, snapshot.getSnapPetType());
                    return serviceText == null ? null : new ServiceItemRespDTO(serviceType, serviceText);
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void validateCreateOrderRequest(CreateOrderReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.addressId() == null || reqDTO.serviceDate() == null
                || reqDTO.serviceType() == null
                || reqDTO.serviceStartTime() == null
                || reqDTO.serviceEndTime() == null
                || reqDTO.finalAmount() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO.finalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO.petIds() == null || reqDTO.petIds().isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (!isSupportedServiceType(reqDTO.serviceType())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (!reqDTO.serviceEndTime().isAfter(reqDTO.serviceStartTime())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO.finalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private void validateQuoteRequest(OrderServiceFeeQuoteReqDTO reqDTO) {
        if (reqDTO == null
                || reqDTO.addressId() == null
                || reqDTO.serviceDate() == null
                || reqDTO.serviceType() == null
                || reqDTO.serviceStartTime() == null
                || reqDTO.serviceEndTime() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO.petIds() == null || reqDTO.petIds().isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (!isSupportedServiceType(reqDTO.serviceType())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (!reqDTO.serviceEndTime().isAfter(reqDTO.serviceStartTime())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private String formatAmount(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private UserAddressDO requireOwnedAddress(Long addressId, Long ownerId) {
        UserAddressDO address = userAddressDao.selectByAddressIdAndUserId(addressId, ownerId);
        if (address == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return address;
    }

    private List<PetArchiveDO> requireOwnedPets(List<Long> petIds, Long ownerId) {
        List<PetArchiveDO> pets = petArchiveDao.selectByIds(petIds).stream()
                .filter(pet -> ownerId.equals(pet.getOwnerId()))
                .toList();
        if (pets.size() != petIds.stream().distinct().count()) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_NOT_FOUND_ERROR);
        }
        return pets;
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    private OrderAddressSnapshotDO buildAddressSnapshot(UserAddressDO address) {
        OrderAddressSnapshotDO snapshot = new OrderAddressSnapshotDO();
        snapshot.setSourceAddressId(address.getAddressId());
        snapshot.setContactName(address.getContactName());
        snapshot.setContactPhone(address.getContactPhone());
        snapshot.setProvince(address.getProvince());
        snapshot.setCity(address.getCity());
        snapshot.setDistrict(address.getDistrict());
        snapshot.setDetailAddress(address.getDetailAddress());
        snapshot.setAddressTag(address.getAddressTag());
        snapshot.setLatitude(address.getLatitude());
        snapshot.setLongitude(address.getLongitude());
        return snapshot;
    }

    private OrderDO buildOrder(CreateOrderReqDTO reqDTO, Long ownerId, Long addressSnapshotId) {
        OrderDO order = new OrderDO();
        order.setOwnerId(ownerId);
        order.setAddressSnapshotId(addressSnapshotId);
        order.setStatus(ORDER_STATUS_BOUNTY);
        order.setTotalAmount(reqDTO.finalAmount());
        order.setServiceDate(reqDTO.serviceDate());
        order.setServiceStartTime(reqDTO.serviceStartTime());
        order.setServiceEndTime(reqDTO.serviceEndTime());
        order.setServiceType(reqDTO.serviceType());
        order.setHardFilterTags(orderHardFilterService.serializeTags(reqDTO.hardFilterTags()));
        order.setRequirementTagsJson(orderRequirementTagService.serialize(reqDTO.requirementTags()));
        return order;
    }

    private SitterProfileDO resolveCaretakerProfileIfPresent() {
        Integer roleType = UserContext.getRoleType();
        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            return null;
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        return sitterProfileDao.selectById(userId);
    }

    private Set<Long> loadAppliedOrderIds(SitterProfileDO viewerProfile) {
        if (viewerProfile == null) {
            return Set.of();
        }
        Long providerId = UserContext.getUserId();
        if (providerId == null) {
            return Set.of();
        }
        return orderApplicationDao.selectApplyingByProviderId(providerId).stream()
                .map(OrderApplicationDO::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String formatServiceTimeSlot(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return TIME_FORMATTER.format(startTime) + "-" + TIME_FORMATTER.format(endTime);
    }

    private OrderPetSnapshotDO buildPetSnapshot(Long orderId, PetArchiveDO pet, String remark) {
        OrderPetSnapshotDO snapshot = new OrderPetSnapshotDO();
        snapshot.setOrderId(orderId);
        snapshot.setArchivePetId(pet.getPetId());
        snapshot.setSnapPetName(pet.getPetName());
        snapshot.setSnapPetType(pet.getPetType());
        snapshot.setSnapReq(remark);
        snapshot.setSnapProfileTagsJson(pet.getProfileTagsJson());
        return snapshot;
    }

    private int normalizePage(Integer page) {
        return page != null && page > 0 ? page : PAGE_DEFAULT;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize != null && pageSize > 0 ? pageSize : PAGE_SIZE_DEFAULT;
    }

    private void validateOpenOrderFilters(Integer petType, Integer serviceType) {
        if (petType != null
                && petType != PET_TYPE_CAT
                && petType != PET_TYPE_DOG
                && petType != PET_TYPE_EXOTIC) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (serviceType != null && !isSupportedServiceType(serviceType)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private boolean isSupportedServiceType(Integer serviceType) {
        return Objects.equals(serviceType, SERVICE_TYPE_FEED) || Objects.equals(serviceType, SERVICE_TYPE_WALK);
    }

    private String serviceTypeToServiceText(Integer serviceType) {
        if (serviceType == null) {
            return null;
        }
        return switch (serviceType) {
            case SERVICE_TYPE_FEED -> "上门喂养";
            case SERVICE_TYPE_WALK -> "上门遛宠";
            default -> null;
        };
    }

    private String buildServiceTypeText(Integer serviceType, Integer petType) {
        String actionText = serviceActionText(serviceType);
        String petTypeText = PetTypeEnum.getDescByCode(petType);
        if (actionText == null || petTypeText == null || petTypeText.isBlank()) {
            return null;
        }
        return "上门" + actionText + petTypeText;
    }

    private String serviceActionText(Integer serviceType) {
        if (serviceType == null) {
            return null;
        }
        return switch (serviceType) {
            case SERVICE_TYPE_FEED -> SERVICE_ACTION_TEXT_FEED;
            case SERVICE_TYPE_WALK -> SERVICE_ACTION_TEXT_WALK;
            default -> null;
        };
    }

    private String toOrderStatusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ORDER_STATUS_BOUNTY -> STATUS_TEXT_BOUNTY;
            default -> null;
        };
    }

    private Integer toIntegerAmount(BigDecimal amount) {
        return amount == null ? 0 : amount.intValue();
    }

    private String formatAddress(OrderAddressSnapshotDO snapshot) {
        if (snapshot == null) {
            return null;
        }
        return String.join("",
                nullToEmpty(snapshot.getProvince()),
                nullToEmpty(snapshot.getCity()),
                nullToEmpty(snapshot.getDistrict()),
                nullToEmpty(snapshot.getDetailAddress()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}
