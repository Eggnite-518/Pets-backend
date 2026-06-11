package com.example.pets_backend.service;

import static com.example.pets_backend.service.CaretakerOrderService.NODE_ARRIVAL;
import static com.example.pets_backend.service.CaretakerOrderService.NODE_CLEAN_LITTER;
import static com.example.pets_backend.service.CaretakerOrderService.NODE_ENTER_HOME;
import static com.example.pets_backend.service.CaretakerOrderService.NODE_FEED_WATER;
import static com.example.pets_backend.service.CaretakerOrderService.NODE_LOCK_LEAVE;
import static com.example.pets_backend.service.CaretakerOrderService.NODE_WALKING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.pets_backend.dto.resp.FulfillmentRecordsRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentUploadRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.FulfillmentMediaStorageService.StoredMedia;
import com.example.pets_backend.service.support.OrderRequirementTagService;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.example.pets_backend.service.support.PetProfileTagService;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import com.example.pets_backend.service.video.AsyncVideoProcessingService;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CaretakerOrderServiceTest {

    @Mock
    private FulfillmentRecordDao fulfillmentRecordDao;
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderAddressSnapshotDao orderAddressSnapshotDao;
    @Mock
    private OrderPetSnapshotDao orderPetSnapshotDao;
    @Mock
    private PetArchiveDao petArchiveDao;
    @Mock
    private UserDao userDao;
    @Mock
    private SitterProfileDao sitterProfileDao;
    @Mock
    private ProviderProfileSupportService providerProfileSupportService;
    @Mock
    private OssAccessibleUrlService ossAccessibleUrlService;
    @Mock
    private CaretakerApplicationService caretakerApplicationService;
    @Mock
    private VideoUploadService videoUploadService;
    @Mock
    private AsyncVideoProcessingService asyncVideoProcessingService;
    @Mock
    private FulfillmentMediaStorageService fulfillmentMediaStorageService;
    @Mock
    private PetProfileTagService petProfileTagService;
    @Mock
    private OrderRequirementTagService orderRequirementTagService;

    @TempDir
    private Path tempDir;

    private CaretakerOrderService caretakerOrderService;

    @BeforeEach
    void setUp() {
        caretakerOrderService = new CaretakerOrderService(
                fulfillmentRecordDao,
                orderDao,
                orderAddressSnapshotDao,
                orderPetSnapshotDao,
                petArchiveDao,
                userDao,
                sitterProfileDao,
                providerProfileSupportService,
                ossAccessibleUrlService,
                caretakerApplicationService,
                videoUploadService,
                asyncVideoProcessingService,
                fulfillmentMediaStorageService,
                petProfileTagService,
                orderRequirementTagService);
        UserContext.setUser(new UserInfoDTO(1002L, "13800000002", "阿周", 2, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void uploadArrivalTransitionsStatusFromPendingToInFulfill() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(fulfillmentRecordDao.selectByOrderId(2002L)).thenReturn(List.of());
        when(fulfillmentMediaStorageService.storeUploadedMedia(eq(2002L), eq(NODE_ARRIVAL), any()))
                .thenReturn(new StoredMedia("object-key", "https://oss.example/a.jpg", 10L, "image/jpeg"));
        doAnswer(invocation -> {
            FulfillmentRecordDO record = invocation.getArgument(0);
            record.setRecordId(10001L);
            return null;
        }).when(fulfillmentRecordDao).insert(any());

        FulfillmentUploadRespDTO result = caretakerOrderService.uploadFulfillment(
                2002L, NODE_ARRIVAL, buildImageFile(), null, null);

        assertEquals(NODE_ARRIVAL, result.nodeType());
        assertEquals("SUCCESS", result.processingStatus());
        verify(orderDao).updateStatus(2002L, 4);
    }

    @Test
    void uploadLockLeaveCreatesProcessingRecordAndSubmitsAsyncTask() {
        MockMultipartFile videoFile = buildVideoFile();
        OrderDO order = buildOrder(2002L, 1002L, 4, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(fulfillmentRecordDao.selectByOrderId(2002L)).thenReturn(List.of(
                buildRecord(10001L, NODE_ARRIVAL),
                buildRecord(10002L, NODE_ENTER_HOME),
                buildRecord(10003L, NODE_FEED_WATER),
                buildRecord(10004L, NODE_CLEAN_LITTER)));
        when(fulfillmentMediaStorageService.saveTempSourceFile(2002L, NODE_LOCK_LEAVE, videoFile))
                .thenReturn(tempDir.resolve("proof.mp4"));
        when(fulfillmentMediaStorageService.buildOriginalObjectKey(2002L, NODE_LOCK_LEAVE, "proof.mp4"))
                .thenReturn("fulfillment/orders/2002/nodes/6/original/proof.mp4");
        doAnswer(invocation -> {
            FulfillmentRecordDO record = invocation.getArgument(0);
            record.setRecordId(10005L);
            return null;
        }).when(fulfillmentRecordDao).insert(any());

        FulfillmentUploadRespDTO result = caretakerOrderService.uploadFulfillment(
                2002L, NODE_LOCK_LEAVE, videoFile, null, null);

        assertEquals(NODE_LOCK_LEAVE, result.nodeType());
        assertEquals("VIDEO", result.mediaType());
        assertEquals("PROCESSING", result.processingStatus());
        verify(videoUploadService).validateVideo(any(), eq(null));
        verify(asyncVideoProcessingService).submit(any());
        verify(orderDao).updateStatus(2002L, 5);
        verify(fulfillmentMediaStorageService, never()).storeUploadedMedia(eq(2002L), eq(NODE_LOCK_LEAVE), any());
    }

    @Test
    void uploadRejectsNodeOutOfSequence() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(fulfillmentRecordDao.selectByOrderId(2002L)).thenReturn(List.of());

        ClientException exception = assertThrows(ClientException.class,
                () -> caretakerOrderService.uploadFulfillment(2002L, NODE_FEED_WATER, buildImageFile(), null, null));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void uploadRejectsWalkingNodeForFeedingOrder() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> caretakerOrderService.uploadFulfillment(2002L, NODE_WALKING, buildImageFile(), null, null));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void uploadRejectsWrongServiceDate() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.of(2026, 6, 1));
        when(orderDao.selectById(2002L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> caretakerOrderService.uploadFulfillment(2002L, NODE_ARRIVAL, buildImageFile(), null, null));

        assertEquals(BaseErrorCode.FULFILLMENT_TIME_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void uploadRejectsMissingLocationForArrival() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> caretakerOrderService.uploadFulfillment(2002L, NODE_ARRIVAL, null, null, null));

        assertEquals(BaseErrorCode.FULFILLMENT_LOCATION_REQUIRED_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void uploadRejectsFarLocation() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderAddressSnapshotDO addressSnapshot = new OrderAddressSnapshotDO();
        addressSnapshot.setSnapshotId(22001L);
        addressSnapshot.setLatitude(31.1886);
        addressSnapshot.setLongitude(121.4365);
        when(orderAddressSnapshotDao.selectById(22001L)).thenReturn(addressSnapshot);

        ClientException exception = assertThrows(ClientException.class,
                () -> caretakerOrderService.uploadFulfillment(2002L, NODE_ARRIVAL, buildImageFile(), 30.0, 120.0));

        assertEquals(BaseErrorCode.FULFILLMENT_LOCATION_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void uploadRejectsFinishedNodeBeforeOrderIsInFulfillment() {
        OrderDO order = buildOrder(2002L, 1002L, 3, 1, 22001L);
        order.setServiceDate(LocalDate.now());
        when(orderDao.selectById(2002L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> caretakerOrderService.uploadFulfillment(2002L, NODE_LOCK_LEAVE, buildVideoFile(), null, null));

        assertEquals(BaseErrorCode.ORDER_FULFILLMENT_STATUS_ERROR.code(), exception.getErrorCode());
        verify(asyncVideoProcessingService, never()).submit(any());
    }

    @Test
    void listFulfillmentRecordsUsesResolvedAccessibleUrl() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1002L, 4, 1, 22001L));
        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setOrderId(2002L);
        record.setNodeType(1);
        record.setMediaType("VIDEO");
        record.setObjectKey("fulfillment/orders/2002/nodes/1/processed/video.mp4");
        record.setProcessingStatus("SUCCESS");
        when(fulfillmentRecordDao.selectByOrderId(2002L)).thenReturn(List.of(record));
        when(fulfillmentMediaStorageService.resolveAccessibleUrl(record))
                .thenReturn("https://signed-url.example/video.mp4");

        FulfillmentRecordsRespDTO result = caretakerOrderService.listFulfillmentRecords(2002L);

        assertEquals(List.of(1, 2, 3, 4, 6), result.checklistNodeTypes());
        assertEquals("https://signed-url.example/video.mp4", result.records().get(0).imageUrl());
        assertEquals(false, result.allNodesCompleted());
    }

    @Test
    void getChecklistReturnsFeedingNodes() {
        assertEquals(List.of(1, 2, 3, 4, 6), caretakerOrderService.getChecklist(1));
    }

    @Test
    void getChecklistReturnsWalkingNodes() {
        assertEquals(List.of(1, 2, 5, 6), caretakerOrderService.getChecklist(2));
    }

    private MockMultipartFile buildImageFile() {
        return new MockMultipartFile("file", "proof.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }

    private MockMultipartFile buildVideoFile() {
        return new MockMultipartFile("file", "proof.mp4", "video/mp4", new byte[] {1, 2, 3});
    }

    private OrderDO buildOrder(Long orderId, Long providerId, Integer status, Integer serviceType,
            Long addressSnapshotId) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(1001L);
        order.setProviderId(providerId);
        order.setStatus(status);
        order.setServiceType(serviceType);
        order.setAddressSnapshotId(addressSnapshotId);
        return order;
    }

    private FulfillmentRecordDO buildRecord(Long recordId, Integer nodeType) {
        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setRecordId(recordId);
        record.setOrderId(2002L);
        record.setNodeType(nodeType);
        return record;
    }
}
