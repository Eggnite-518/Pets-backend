package com.example.pets_backend.service;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.PetArchiveDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.service.support.OrderRequirementTagService;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.example.pets_backend.service.support.PetProfileTagService;
import com.example.pets_backend.service.support.FulfillmentCompletionSupport;
import com.example.pets_backend.service.support.FulfillmentRecordQuerySupport;
import com.example.pets_backend.service.support.OrderAddressFormatSupport;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import com.example.pets_backend.dto.resp.CaretakerOrderDetailRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordsRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentUploadRespDTO;
import com.example.pets_backend.dto.resp.ServiceItemRespDTO;
import com.example.pets_backend.enums.OrderStatusEnum;
import com.example.pets_backend.enums.PetTypeEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.fulfillment.AdvanceOrderStatusStep;
import com.example.pets_backend.service.fulfillment.FulfillmentContext;
import com.example.pets_backend.service.fulfillment.FulfillmentNodeType;
import com.example.pets_backend.service.fulfillment.FulfillmentStepKey;
import com.example.pets_backend.service.fulfillment.FulfillmentStepRegistry;
import com.example.pets_backend.service.fulfillment.LoadAssignedOrderStep;
import com.example.pets_backend.service.fulfillment.PersistRecordStep;
import com.example.pets_backend.service.fulfillment.ServiceFulfillmentFlow;
import com.example.pets_backend.service.fulfillment.StoreDirectMediaStep;
import com.example.pets_backend.service.fulfillment.StoreVideoSourceStep;
import com.example.pets_backend.service.fulfillment.SubmitVideoProcessingStep;
import com.example.pets_backend.service.fulfillment.ValidateFulfillmentStatusStep;
import com.example.pets_backend.service.fulfillment.ValidateLocationStep;
import com.example.pets_backend.service.fulfillment.ValidateNotDuplicateStep;
import com.example.pets_backend.service.fulfillment.ValidateSequenceStep;
import com.example.pets_backend.service.fulfillment.ValidateServiceDateStep;
import com.example.pets_backend.service.fulfillment.ValidateServiceNodeStep;
import com.example.pets_backend.service.video.AsyncVideoProcessingService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CaretakerOrderService {

    static final int NODE_ARRIVAL = 1;
    static final int NODE_ENTER_HOME = 2;
    static final int NODE_FEED_WATER = 3;
    static final int NODE_CLEAN_LITTER = 4;
    static final int NODE_WALKING = 5;
    static final int NODE_LOCK_LEAVE = 6;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final OrderDao orderDao;
    private final OrderAddressSnapshotDao orderAddressSnapshotDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final PetArchiveDao petArchiveDao;
    private final UserDao userDao;
    private final SitterProfileDao sitterProfileDao;
    private final ProviderProfileSupportService providerProfileSupportService;
    private final OssAccessibleUrlService ossAccessibleUrlService;
    private final CaretakerApplicationService caretakerApplicationService;
    private final FulfillmentMediaStorageService fulfillmentMediaStorageService;
    private final PetProfileTagService petProfileTagService;
    private final OrderRequirementTagService orderRequirementTagService;
    private final FulfillmentStepRegistry stepRegistry;

    public CaretakerOrderService(FulfillmentRecordDao fulfillmentRecordDao,
            OrderDao orderDao,
            OrderAddressSnapshotDao orderAddressSnapshotDao,
            OrderPetSnapshotDao orderPetSnapshotDao,
            PetArchiveDao petArchiveDao,
            UserDao userDao,
            SitterProfileDao sitterProfileDao,
            ProviderProfileSupportService providerProfileSupportService,
            OssAccessibleUrlService ossAccessibleUrlService,
            CaretakerApplicationService caretakerApplicationService,
            VideoUploadService videoUploadService,
            AsyncVideoProcessingService asyncVideoProcessingService,
            FulfillmentMediaStorageService fulfillmentMediaStorageService,
            PetProfileTagService petProfileTagService,
            OrderRequirementTagService orderRequirementTagService) {
        this.fulfillmentRecordDao = fulfillmentRecordDao;
        this.orderDao = orderDao;
        this.orderAddressSnapshotDao = orderAddressSnapshotDao;
        this.orderPetSnapshotDao = orderPetSnapshotDao;
        this.petArchiveDao = petArchiveDao;
        this.userDao = userDao;
        this.sitterProfileDao = sitterProfileDao;
        this.providerProfileSupportService = providerProfileSupportService;
        this.ossAccessibleUrlService = ossAccessibleUrlService;
        this.caretakerApplicationService = caretakerApplicationService;
        this.fulfillmentMediaStorageService = fulfillmentMediaStorageService;
        this.petProfileTagService = petProfileTagService;
        this.orderRequirementTagService = orderRequirementTagService;
        this.stepRegistry = new FulfillmentStepRegistry(List.of(
                new LoadAssignedOrderStep(orderDao),
                new ValidateServiceNodeStep(),
                new ValidateFulfillmentStatusStep(),
                new ValidateServiceDateStep(),
                new ValidateLocationStep(orderAddressSnapshotDao),
                new ValidateSequenceStep(fulfillmentRecordDao),
                new ValidateNotDuplicateStep(fulfillmentRecordDao),
                new StoreDirectMediaStep(fulfillmentMediaStorageService),
                new StoreVideoSourceStep(videoUploadService, fulfillmentMediaStorageService),
                new PersistRecordStep(fulfillmentRecordDao),
                new AdvanceOrderStatusStep(orderDao, fulfillmentRecordDao),
                new SubmitVideoProcessingStep(asyncVideoProcessingService)));
    }

    @Transactional
    public FulfillmentUploadRespDTO uploadFulfillment(Long orderId, Integer nodeType, MultipartFile file,
            Double lat, Double lng) {
        FulfillmentNodeType node = FulfillmentNodeType.fromCode(nodeType);
        FulfillmentContext context = new FulfillmentContext(orderId, node, file, lat, lng);
        for (FulfillmentStepKey stepKey : node.steps()) {
            stepRegistry.get(stepKey).handle(context);
        }
        return context.toResponse();
    }

    public List<Integer> getChecklist(Integer serviceType) {
        return ServiceFulfillmentFlow.fromServiceType(serviceType).nodeCodes();
    }

    public FulfillmentRecordsRespDTO listFulfillmentRecords(Long orderId) {
        FulfillmentContext context = new FulfillmentContext(
                orderId, FulfillmentNodeType.ARRIVAL, null, null, null);
        new LoadAssignedOrderStep(orderDao).handle(context);
        OrderDO order = context.order();
        List<Integer> checklistNodeTypes = ServiceFulfillmentFlow
                .fromServiceType(order.getServiceType())
                .nodeCodes();
        List<FulfillmentRecordRespDTO> records = FulfillmentRecordQuerySupport
                .pickLatestDisplayRecords(fulfillmentRecordDao.selectByOrderId(orderId))
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
                        record.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(record.getCreatedAt())))
                .toList();
        boolean allNodesCompleted = FulfillmentCompletionSupport
                .areAllChecklistNodesUploaded(order, fulfillmentRecordDao);
        return new FulfillmentRecordsRespDTO(checklistNodeTypes, records, allNodesCompleted);
    }

    /**
     * 宠托师查看订单详情。
     * 权限：订单为悬赏中（status=1）任意宠托师可查看；其他状态仅指派的宠托师可查看。
     */
    public CaretakerOrderDetailRespDTO getOrderDetail(Long orderId) {
        Long currentUserId = UserContext.getUserId();
        OrderDO order = orderDao.selectById(orderId);
        if (order == null || !Objects.equals(order.getDeleted(), 0)) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }

        // 权限校验：悬赏中任何宠托师均可查看；否则只有指派的宠托师可查看
        boolean isOpen = Objects.equals(order.getStatus(), 1);
        boolean isAssigned = Objects.equals(order.getProviderId(), currentUserId);
        if (!isOpen && !isAssigned) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }

        // 地址
        OrderAddressSnapshotDO addressSnap = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        CaretakerOrderDetailRespDTO.AddressDTO address = buildAddressDTO(addressSnap);

        // 距离：宠托师常驻坐标 → 订单地址坐标（与接单大厅使用同一计算方法）
        SitterProfileDO profile = sitterProfileDao.selectById(currentUserId);
        Double distanceKm = providerProfileSupportService.resolveDistanceKm(addressSnap, profile);

        // 宠主信息
        UserDO owner = userDao.selectById(order.getOwnerId());
        CaretakerOrderDetailRespDTO.OwnerDTO ownerDTO = buildOwnerDTO(owner);

        // 宠物快照
        List<OrderPetSnapshotDO> petSnaps = orderPetSnapshotDao.selectByOrderIds(List.of(orderId));
        Map<Long, PetArchiveDO> petArchives = petArchiveDao.selectByIds(petSnaps.stream()
                        .map(OrderPetSnapshotDO::getArchivePetId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(PetArchiveDO::getPetId, Function.identity(), (a, b) -> a));
        List<CaretakerOrderDetailRespDTO.PetDTO> pets = petSnaps.stream()
                .map(snapshot -> buildPetDTO(snapshot, petArchives.get(snapshot.getArchivePetId())))
                .toList();

        // 服务类型（去重）
        List<ServiceItemRespDTO> serviceItems = petSnaps.stream()
                .map(s -> toServiceItem(s.getSnapPetType()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 已完成打卡节点
        List<Integer> completedNodeTypes = fulfillmentRecordDao.selectByOrderId(orderId).stream()
                .map(FulfillmentRecordDO::getNodeType)
                .toList();

        String statusText = OrderStatusEnum.getDescByCode(order.getStatus());
        String timeSlot = formatTimeSlot(order.getServiceStartTime(), order.getServiceEndTime());

        boolean hasApplied = caretakerApplicationService.hasApplied(currentUserId, orderId);
        boolean revealAccessNote = isAssigned && isFulfillmentPhase(order.getStatus());
        var requirementTags = revealAccessNote
                ? orderRequirementTagService.toRespDTO(order.getRequirementTagsJson())
                : orderRequirementTagService.toDetailRespDTO(order.getRequirementTagsJson());

        return new CaretakerOrderDetailRespDTO(
                order.getOrderId() == null ? null : order.getOrderId().toString(),
                order.getStatus(),
                statusText,
                serviceItems,
                order.getServiceDate() == null ? null : order.getServiceDate().toString(),
                timeSlot,
                order.getTotalAmount() == null ? "0.00" : order.getTotalAmount().toPlainString(),
                address,
                ownerDTO,
                pets,
                resolveServiceNotes(petSnaps, petArchives),
                requirementTags,
                completedNodeTypes,
                getChecklist(order.getServiceType()),
                order.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(order.getCreatedAt()),
                distanceKm,
                hasApplied);
    }

    private CaretakerOrderDetailRespDTO.AddressDTO buildAddressDTO(OrderAddressSnapshotDO snap) {
        if (snap == null) {
            return new CaretakerOrderDetailRespDTO.AddressDTO("", "", null, null);
        }
        return new CaretakerOrderDetailRespDTO.AddressDTO(
                OrderAddressFormatSupport.formatFullAddress(snap),
                OrderAddressFormatSupport.formatCityDistrict(snap),
                snap.getLatitude(),
                snap.getLongitude());
    }

    private CaretakerOrderDetailRespDTO.OwnerDTO buildOwnerDTO(UserDO user) {
        if (user == null) {
            return new CaretakerOrderDetailRespDTO.OwnerDTO("", "", "");
        }
        return new CaretakerOrderDetailRespDTO.OwnerDTO(
                nullToEmpty(user.getNickname()),
                nullToEmpty(ossAccessibleUrlService.toDisplayUrl(user.getAvatarUrl())),
                maskPhone(user.getPhone()));
    }

    private CaretakerOrderDetailRespDTO.PetDTO buildPetDTO(OrderPetSnapshotDO snap, PetArchiveDO archive) {
        String petTypeText = PetTypeEnum.getDescByCode(snap.getSnapPetType());
        String profileTagsJson = snap.getSnapProfileTagsJson();
        if (profileTagsJson == null && archive != null) {
            profileTagsJson = archive.getProfileTagsJson();
        }
        var profileTags = petProfileTagService.toRespDTO(profileTagsJson);
        return new CaretakerOrderDetailRespDTO.PetDTO(
                snap.getArchivePetId() == null ? "" : snap.getArchivePetId().toString(),
                nullToEmpty(snap.getSnapPetName()),
                snap.getSnapPetType(),
                nullToEmpty(petTypeText),
                "",
                profileTags == null ? "" : nullToEmpty(profileTags.ageGroupDesc()),
                archive == null ? "" : nullToEmpty(ossAccessibleUrlService.toDisplayUrl(archive.getImage())),
                resolveCareNotes(snap, archive),
                profileTags);
    }

    private ServiceItemRespDTO toServiceItem(Integer petType) {
        if (petType == null) return null;
        return switch (petType) {
            case 1 -> new ServiceItemRespDTO(1, "上门喂猫");
            case 2 -> new ServiceItemRespDTO(2, "上门遛狗");
            case 3 -> new ServiceItemRespDTO(1, "上门喂养异宠");
            default -> null;
        };
    }

    private String formatTimeSlot(java.time.LocalTime start, java.time.LocalTime end) {
        if (start == null || end == null) return "";
        java.time.format.DateTimeFormatter tf = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return tf.format(start) + "-" + tf.format(end);
    }

    private String resolveServiceNotes(List<OrderPetSnapshotDO> petSnaps, Map<Long, PetArchiveDO> petArchives) {
        String snapshotNotes = petSnaps.stream()
                .map(OrderPetSnapshotDO::getSnapReq)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(note -> !note.isEmpty())
                .distinct()
                .collect(Collectors.joining("\n"));
        if (!snapshotNotes.isEmpty()) {
            return snapshotNotes;
        }
        return petSnaps.stream()
                .map(OrderPetSnapshotDO::getArchivePetId)
                .map(petArchives::get)
                .filter(Objects::nonNull)
                .map(PetArchiveDO::getDefaultReq)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(note -> !note.isEmpty())
                .distinct()
                .collect(Collectors.joining("\n"));
    }

    private String resolveCareNotes(OrderPetSnapshotDO snap, PetArchiveDO archive) {
        if (archive != null && archive.getDefaultReq() != null && !archive.getDefaultReq().isBlank()) {
            return archive.getDefaultReq().trim();
        }
        return nullToEmpty(snap.getSnapReq());
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return nullToEmpty(phone);
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static boolean isFulfillmentPhase(Integer status) {
        return status != null
                && status >= OrderStatusEnum.PENDING_FULFILLMENT.getCode();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
