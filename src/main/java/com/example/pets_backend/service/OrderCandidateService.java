package com.example.pets_backend.service;

import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.CandidateListItemRespDTO;
import com.example.pets_backend.dto.resp.CandidateListRespDTO;
import com.example.pets_backend.dto.resp.ProviderDetailRespDTO;
import com.example.pets_backend.dto.resp.ServiceItemRespDTO;
import com.example.pets_backend.enums.CandidateSortByEnum;
import com.example.pets_backend.enums.OrderApplicationStatusEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OrderHardFilterService;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import com.example.pets_backend.service.support.ProviderPublicMetrics;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCandidateService {

    private static final int ORDER_STATUS_BOUNTY = 1;
    private static final int PET_TYPE_CAT = 1;
    private static final int PET_TYPE_DOG = 2;
    private static final int PET_TYPE_EXOTIC = 3;
    private static final int SERVICE_TYPE_FEED_CAT = 1;
    private static final int SERVICE_TYPE_WALK_DOG = 2;
    private static final String SERVICE_TEXT_FEED_CAT = "上门喂猫";
    private static final String SERVICE_TEXT_WALK_DOG = "上门遛狗";
    private static final String SERVICE_TEXT_FEED_EXOTIC = "上门喂养异宠";
    private static final String STATUS_TEXT_BOUNTY = "等待反馈";

    private final OrderDao orderDao;
    private final OrderApplicationDao orderApplicationDao;
    private final OrderAddressSnapshotDao orderAddressSnapshotDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final UserDao userDao;
    private final SitterProfileDao sitterProfileDao;
    private final ProviderProfileSupportService providerProfileSupportService;
    private final OrderHardFilterService orderHardFilterService;

    public CandidateListRespDTO listCandidates(Long orderId, String sortBy) {
        CandidateSortByEnum sortByEnum = CandidateSortByEnum.fromCode(sortBy);
        OrderDO order = requireOwnerBountyOrder(orderId);

        List<OrderApplicationDO> applications = orderApplicationDao.selectApplyingByOrderId(orderId);
        if (applications.isEmpty()) {
            return new CandidateListRespDTO(orderId, sortByEnum.getCode(), sortByEnum.getDesc(), List.of());
        }

        List<Long> providerIds = applications.stream()
                .map(OrderApplicationDO::getProviderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserDO> userMap = userDao.selectByIds(providerIds).stream()
                .collect(Collectors.toMap(UserDO::getUserId, Function.identity()));
        Map<Long, SitterProfileDO> profileMap = sitterProfileDao.selectByIds(providerIds).stream()
                .collect(Collectors.toMap(SitterProfileDO::getProviderId, Function.identity()));

        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        List<OrderApplicationDO> eligibleApplications = applications.stream()
                .filter(application -> orderHardFilterService.isProviderEligible(
                        profileMap.get(application.getProviderId()), addressSnapshot, order))
                .toList();
        if (eligibleApplications.isEmpty()) {
            return new CandidateListRespDTO(orderId, sortByEnum.getCode(), sortByEnum.getDesc(), List.of());
        }

        List<Long> eligibleProviderIds = eligibleApplications.stream()
                .map(OrderApplicationDO::getProviderId)
                .distinct()
                .toList();
        Map<Long, ProviderPublicMetrics> metricsMap = eligibleProviderIds.stream()
                .collect(Collectors.toMap(Function.identity(), providerProfileSupportService::resolveMetrics));

        List<CandidateListItemRespDTO> candidates = eligibleApplications.stream()
                .map(application -> {
                    Long providerId = application.getProviderId();
                    UserDO user = userMap.get(providerId);
                    SitterProfileDO profile = profileMap.get(providerId);
                    ProviderPublicMetrics metrics = metricsMap.get(providerId);
                    return new CandidateListItemRespDTO(
                            application.getApplyId(),
                            providerId,
                            user == null ? null : user.getNickname(),
                            user == null ? null : user.getAvatarUrl(),
                            application.getApplyStatus(),
                            OrderApplicationStatusEnum.getDescByCode(application.getApplyStatus()),
                            providerProfileSupportService.resolveDistanceKm(addressSnapshot, profile),
                            metrics.rating(),
                            metrics.totalOrderCount(),
                            metrics.creditScore());
                })
                .sorted(buildComparator(sortByEnum))
                .toList();

        return new CandidateListRespDTO(orderId, sortByEnum.getCode(), sortByEnum.getDesc(), candidates);
    }

    public ProviderDetailRespDTO getProviderDetail(Long orderId, Long providerId) {
        OrderDO order = requireOwnerOrder(orderId);

        OrderApplicationDO application = orderApplicationDao.selectByOrderIdAndProviderId(orderId, providerId);
        if (application == null) {
            throw new ClientException(BaseErrorCode.ORDER_APPLICATION_NOT_FOUND_ERROR);
        }

        UserDO user = userDao.selectById(providerId);
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        if (Objects.equals(order.getStatus(), ORDER_STATUS_BOUNTY)) {
            orderHardFilterService.ensureProviderEligible(profile, addressSnapshot, order);
        }
        ProviderPublicMetrics metrics = providerProfileSupportService.resolveMetrics(providerId);

        List<OrderPetSnapshotDO> snapshots = orderPetSnapshotDao.selectByOrderIds(List.of(orderId));
        OrderPetSnapshotDO firstSnapshot = snapshots.isEmpty() ? null : snapshots.get(0);

        return new ProviderDetailRespDTO(
                application.getApplyId(),
                orderId,
                order.getStatus(),
                toOrderStatusText(order.getStatus()),
                buildServiceItems(snapshots),
                toIntegerAmount(order.getTotalAmount()),
                providerProfileSupportService.resolveDistanceKm(addressSnapshot, profile),
                firstSnapshot == null ? null : firstSnapshot.getSnapPetName(),
                null,
                firstSnapshot == null ? null : firstSnapshot.getSnapReq(),
                providerId,
                user == null ? null : user.getNickname(),
                user == null ? null : user.getAvatarUrl(),
                metrics.creditScore(),
                metrics.rating(),
                metrics.totalOrderCount(),
                metrics.complianceRate(),
                metrics.levelTag(),
                metrics.certLabels(),
                metrics.reviewCount(),
                providerProfileSupportService.resolvePunctualityAvg(providerId),
                providerProfileSupportService.resolveProfessionalAvg(providerId));
    }

    private Comparator<CandidateListItemRespDTO> buildComparator(CandidateSortByEnum sortBy) {
        return switch (sortBy) {
            case RATING -> Comparator
                    .comparing(CandidateListItemRespDTO::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(CandidateListItemRespDTO::applicationId, Comparator.nullsLast(Comparator.naturalOrder()));
            case TOTAL_ORDERS -> Comparator
                    .comparing(CandidateListItemRespDTO::totalOrderCount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(CandidateListItemRespDTO::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(CandidateListItemRespDTO::applicationId, Comparator.nullsLast(Comparator.naturalOrder()));
            case DISTANCE -> Comparator
                    .comparing(CandidateListItemRespDTO::distanceKm, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(CandidateListItemRespDTO::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(CandidateListItemRespDTO::applicationId, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private OrderDO requireOwnerOrder(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        return order;
    }

    private OrderDO requireOwnerBountyOrder(Long orderId) {
        OrderDO order = requireOwnerOrder(orderId);
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_BOUNTY)) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OPEN_ERROR);
        }
        return order;
    }

    private List<ServiceItemRespDTO> buildServiceItems(List<OrderPetSnapshotDO> snapshots) {
        return snapshots.stream()
                .map(snapshot -> {
                    Integer serviceType = petTypeToServiceType(snapshot.getSnapPetType());
                    String serviceText = petTypeToServiceText(snapshot.getSnapPetType());
                    return serviceType == null ? null : new ServiceItemRespDTO(serviceType, serviceText);
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Integer petTypeToServiceType(Integer petType) {
        if (petType == null) {
            return null;
        }
        return switch (petType) {
            case PET_TYPE_CAT -> SERVICE_TYPE_FEED_CAT;
            case PET_TYPE_DOG -> SERVICE_TYPE_WALK_DOG;
            case PET_TYPE_EXOTIC -> SERVICE_TYPE_FEED_CAT;
            default -> null;
        };
    }

    private String petTypeToServiceText(Integer petType) {
        if (petType == null) {
            return null;
        }
        return switch (petType) {
            case PET_TYPE_CAT -> SERVICE_TEXT_FEED_CAT;
            case PET_TYPE_DOG -> SERVICE_TEXT_WALK_DOG;
            case PET_TYPE_EXOTIC -> SERVICE_TEXT_FEED_EXOTIC;
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

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}
