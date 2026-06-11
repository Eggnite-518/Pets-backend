package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.PetArchiveDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.UserAddressDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.CreateOrderReqDTO;
import com.example.pets_backend.dto.req.OrderRequirementTagsReqDTO;
import com.example.pets_backend.dto.req.OrderServiceFeeQuoteReqDTO;
import com.example.pets_backend.dto.resp.CreateOrderRespDTO;
import com.example.pets_backend.dto.resp.MyRewardingOrderRespDTO;
import com.example.pets_backend.dto.resp.OpenOrderPageRespDTO;
import com.example.pets_backend.dto.resp.OrderServiceFeeQuoteRespDTO;
import com.example.pets_backend.dto.resp.ProviderDetailRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.service.support.OrderHardFilterService;
import com.example.pets_backend.service.support.OrderRequirementTagService;
import com.example.pets_backend.service.support.OrderServiceFeeCalculator;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.example.pets_backend.service.support.PetProfileTagService;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderApplicationDao orderApplicationDao;

    @Mock
    private OrderAddressSnapshotDao orderAddressSnapshotDao;

    @Mock
    private OrderPetSnapshotDao orderPetSnapshotDao;

    @Mock
    private PetArchiveDao petArchiveDao;

    @Mock
    private UserAddressDao userAddressDao;

    @Mock
    private UserDao userDao;

    @Mock
    private OrderSettlementService orderSettlementService;

    @Mock
    private OrderCandidateService orderCandidateService;

    @Mock
    private OrderHardFilterService orderHardFilterService;

    @Mock
    private OrderBountyPushService orderBountyPushService;

    @Mock
    private OrderOfficialNotificationService orderOfficialNotificationService;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private ProviderProfileSupportService providerProfileSupportService;

    @Mock
    private OssAccessibleUrlService ossAccessibleUrlService;

    private OrderRequirementTagService orderRequirementTagService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        orderRequirementTagService = new OrderRequirementTagService(objectMapper);
        OrderServiceFeeCalculator orderServiceFeeCalculator = new OrderServiceFeeCalculator(
                orderRequirementTagService,
                new PetProfileTagService(objectMapper));
        orderService = new OrderService(orderDao, orderApplicationDao, orderAddressSnapshotDao, orderPetSnapshotDao,
                petArchiveDao, userAddressDao, userDao, orderSettlementService, orderCandidateService,
                orderHardFilterService, null, null, orderRequirementTagService, orderServiceFeeCalculator,
                orderBountyPushService, orderOfficialNotificationService, sitterProfileDao, providerProfileSupportService,
                ossAccessibleUrlService);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void listMyRewardingReturnsOrdersWithPetsAndApplications() {
        OrderDO order = buildOrder(2002L, 1001L, 1);
        when(orderDao.selectByOwnerIdAndStatus(1001L, 1)).thenReturn(List.of(order));

        OrderPetSnapshotDO snapshot = buildSnapshot(8001L, 2002L, 3001L, "团团");
        when(orderPetSnapshotDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of(snapshot));

        OrderApplicationDO application = buildApplication(4002L, 2002L, 1002L, 0);
        when(orderApplicationDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of(application));

        UserDO user = buildUser(1002L, "阿周", "https://example.com/avatar/1002.png");
        when(userDao.selectByIds(List.of(1002L))).thenReturn(List.of(user));

        List<MyRewardingOrderRespDTO> result = orderService.listMyRewarding();

        assertEquals(1, result.size());
        assertEquals(2002L, result.get(0).orderId());
        assertEquals(1, result.get(0).pets().size());
        assertEquals("团团", result.get(0).pets().get(0).petName());
        assertEquals(1, result.get(0).pets().get(0).petType());
        assertEquals(1, result.get(0).applications().size());
        assertEquals("阿周", result.get(0).applications().get(0).providerNickname());
        assertEquals("待处理", result.get(0).statusDesc());
    }

    @Test
    void listMyRewardingReturnsEmptyListWhenNoOrders() {
        when(orderDao.selectByOwnerIdAndStatus(1001L, 1)).thenReturn(List.of());

        List<MyRewardingOrderRespDTO> result = orderService.listMyRewarding();

        assertTrue(result.isEmpty());
    }

    @Test
    void listMyRewardingRejectsMissingUserContext() {
        UserContext.clear();

        ClientException exception = assertThrows(ClientException.class,
                () -> orderService.listMyRewarding());

        assertEquals(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void quoteDoesNotReturnSpecialTagFee() {
        UserAddressDO address = buildAddress(6001L, 1001L, "上海");
        when(userAddressDao.selectByAddressIdAndUserId(6001L, 1001L)).thenReturn(address);

        PetArchiveDO pet = buildPetArchive(3001L, "团团", 1);
        pet.setOwnerId(1001L);
        when(petArchiveDao.selectByIds(List.of(3001L))).thenReturn(List.of(pet));

        OrderServiceFeeQuoteRespDTO result = orderService.quote(new OrderServiceFeeQuoteReqDTO(
                List.of(3001L),
                1,
                6001L,
                LocalDate.of(2026, 5, 6),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                null,
                null,
                null));

        assertEquals("40.00", result.totalAmount());
        assertEquals(7, result.priceItems().size());
        assertEquals(0, result.priceItems().get(1).quantity());
        assertEquals(0, result.priceItems().get(4).quantity());
        assertTrue(result.priceItems().stream().noneMatch(item -> item.itemName().contains("特殊标签")));
    }

    @Test
    void quoteIncludesPlayCompanionFeeWhenSelected() {
        UserAddressDO address = buildAddress(6001L, 1001L, "上海");
        when(userAddressDao.selectByAddressIdAndUserId(6001L, 1001L)).thenReturn(address);

        PetArchiveDO pet = buildPetArchive(3001L, "团团", 1);
        pet.setOwnerId(1001L);
        when(petArchiveDao.selectByIds(List.of(3001L))).thenReturn(List.of(pet));

        OrderServiceFeeQuoteRespDTO result = orderService.quote(new OrderServiceFeeQuoteReqDTO(
                List.of(3001L),
                1,
                6001L,
                LocalDate.of(2026, 5, 6),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                null,
                null,
                new OrderRequirementTagsReqDTO(List.of("NEED_PLAY_COMPANION"), null, null, null)));

        assertEquals("45.00", result.totalAmount());
        assertEquals(1, result.priceItems().get(4).quantity());
        assertEquals("陪玩增值费", result.priceItems().get(4).itemName());
    }

    @Test
    void createUsesFrontendFinalAmount() {
        UserAddressDO address = buildAddress(6001L, 1001L, "上海");
        when(userAddressDao.selectByAddressIdAndUserId(6001L, 1001L)).thenReturn(address);

        PetArchiveDO pet = buildPetArchive(3001L, "团团", 1);
        pet.setOwnerId(1001L);
        when(petArchiveDao.selectByIds(List.of(3001L))).thenReturn(List.of(pet));
        when(orderHardFilterService.serializeTags(any())).thenReturn(null);
        when(orderHardFilterService.parseTags(any(OrderDO.class))).thenReturn(List.of());

        doAnswer(invocation -> {
            OrderAddressSnapshotDO snapshot = invocation.getArgument(0);
            snapshot.setSnapshotId(7001L);
            return 1;
        }).when(orderAddressSnapshotDao).insert(any(OrderAddressSnapshotDO.class));

        doAnswer(invocation -> {
            OrderDO order = invocation.getArgument(0);
            order.setOrderId(2002L);
            return 1;
        }).when(orderDao).insert(any(OrderDO.class));

        CreateOrderRespDTO result = orderService.create(new CreateOrderReqDTO(
                6001L,
                List.of(3001L),
                1,
                LocalDate.of(2026, 5, 6),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                BigDecimal.valueOf(88),
                "请拍照反馈",
                null,
                null));

        assertEquals(2002L, result.orderId());
        assertEquals(new BigDecimal("88.00"), result.totalAmount());

        ArgumentCaptor<OrderDO> orderCaptor = ArgumentCaptor.forClass(OrderDO.class);
        verify(orderDao).insert(orderCaptor.capture());
        assertEquals(new BigDecimal("88.00"), orderCaptor.getValue().getTotalAmount());
        assertEquals(1, orderCaptor.getValue().getServiceType());
        verify(orderBountyPushService).notifyEligibleProviders(any(OrderDO.class));
    }

    @Test
    void createAcceptsServiceWindowAndServiceType() {
        UserAddressDO address = new UserAddressDO();
        address.setAddressId(5001L);
        address.setUserId(1001L);
        when(userAddressDao.selectByAddressIdAndUserId(5001L, 1001L)).thenReturn(address);

        PetArchiveDO pet1 = buildPetArchive(3001L, "团团", 1);
        pet1.setOwnerId(1001L);
        PetArchiveDO pet2 = buildPetArchive(3002L, "圆圆", 1);
        pet2.setOwnerId(1001L);
        when(petArchiveDao.selectByIds(List.of(3001L, 3002L))).thenReturn(List.of(pet1, pet2));
        when(orderHardFilterService.serializeTags(any())).thenReturn(null);
        when(orderHardFilterService.parseTags(any(OrderDO.class))).thenReturn(List.of());

        CreateOrderReqDTO reqDTO = new CreateOrderReqDTO(
                5001L,
                List.of(3001L, 3002L),
                1,
                LocalDate.of(2026, 5, 25),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                new BigDecimal("80.00"),
                "猫粮在厨房柜子里，请换新水并拍照反馈",
                null,
                null);

        CreateOrderRespDTO result = orderService.create(reqDTO);

        assertEquals(new BigDecimal("80.00"), result.totalAmount());
        verify(orderAddressSnapshotDao).insert(any(OrderAddressSnapshotDO.class));
        verify(orderDao).insert(any(OrderDO.class));
        verify(orderPetSnapshotDao, times(2)).insert(any(OrderPetSnapshotDO.class));
    }

    @Test
    void getProviderDetailDelegatesToOrderCandidateService() {
        ProviderDetailRespDTO expected = new ProviderDetailRespDTO(
                4002L,
                2002L,
                1,
                "等待反馈",
                List.of(),
                98,
                0.8,
                "团团",
                null,
                "memo",
                1002L,
                "阿周",
                "https://example.com/avatar.png",
                92,
                4.8,
                28,
                96.5,
                "银牌宠托师",
                List.of("实名认证"),
                15,
                4.7,
                4.9);
        when(orderCandidateService.getProviderDetail(2002L, 1002L)).thenReturn(expected);

        ProviderDetailRespDTO result = orderService.getProviderDetail(2002L, 1002L);

        assertEquals(expected, result);
        verify(orderCandidateService).getProviderDetail(2002L, 1002L);
    }

    @Test
    void listOpenOrdersReturnsPagedResult() {
        OrderDO order = buildOrder(2002L, 1001L, 1);
        IPage<OrderDO> orderPage = new Page<>(1, 10, 1);
        orderPage.setRecords(List.of(order));
        when(orderDao.selectOpenOrderPage(1, 10)).thenReturn(orderPage);

        OrderPetSnapshotDO snapshot = buildSnapshot(8001L, 2002L, 3001L, "团团");
        when(orderPetSnapshotDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of(snapshot));

        OrderApplicationDO application = buildApplication(4002L, 2002L, 1002L, 0);
        when(orderApplicationDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of(application));
        when(orderHardFilterService.parseTags(any(OrderDO.class))).thenReturn(List.of());

        OpenOrderPageRespDTO result = orderService.listOpenOrders(null, 1, 10);

        assertEquals(1, result.list().size());
        assertEquals("团团", result.list().get(0).pets().get(0).petName());
        assertEquals(1, result.list().get(0).applicationCount());
        assertEquals("14:00-15:00", result.list().get(0).serviceTimeSlot());
    }

    @Test
    void listOpenOrdersFiltersByServiceTypeInDatabase() {
        OrderDO order = buildOrder(2002L, 1001L, 1);
        IPage<OrderDO> orderPage = new Page<>(1, 10, 1);
        orderPage.setRecords(List.of(order));
        when(orderDao.selectOpenOrderPageByPetType(1, 10, 1)).thenReturn(orderPage);

        OrderPetSnapshotDO snapshot = buildSnapshot(8001L, 2002L, 3001L, "团团");
        when(orderPetSnapshotDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of(snapshot));
        when(orderApplicationDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of());
        when(orderHardFilterService.parseTags(any(OrderDO.class))).thenReturn(List.of());

        OpenOrderPageRespDTO result = orderService.listOpenOrders(1, 1, 10);

        assertEquals(1, result.total());
        assertEquals(1, result.list().size());
    }

    @Test
    void listOpenOrdersReturnsEmptyPage() {
        IPage<OrderDO> emptyPage = new Page<>(2, 10, 11);
        emptyPage.setRecords(List.of());
        when(orderDao.selectOpenOrderPage(2, 10)).thenReturn(emptyPage);

        OpenOrderPageRespDTO result = orderService.listOpenOrders(null, 2, 10);

        assertEquals(11, result.total());
        assertTrue(result.list().isEmpty());
    }

    @Test
    void selectProviderUpdatesOrderAndApplications() {
        OrderDO order = buildOrder(2002L, 1001L, 1);
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderApplicationDO application = buildApplication(4002L, 2002L, 1002L, 0);
        when(orderApplicationDao.selectByOrderIdAndProviderId(2002L, 1002L)).thenReturn(application);

        when(orderDao.updateProviderIdAndStatus(2002L, 1002L, 2)).thenReturn(1);
        when(orderApplicationDao.updateApplyStatus(2002L, 1002L, 2)).thenReturn(1);
        when(orderApplicationDao.updateApplyStatusForOthers(2002L, 1002L, 1)).thenReturn(1);

        assertDoesNotThrow(() -> orderService.selectProvider(2002L, 1002L));

        verify(orderDao).updateProviderIdAndStatus(2002L, 1002L, 2);
        verify(orderApplicationDao).updateApplyStatus(2002L, 1002L, 2);
        verify(orderApplicationDao).updateApplyStatusForOthers(2002L, 1002L, 1);
        verify(orderOfficialNotificationService).notifyCaretakerOnSelected(order, 1002L);
    }

    @Test
    void selectProviderRejectsNonOwner() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1003L, 1));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderService.selectProvider(2002L, 1002L));

        assertEquals(BaseErrorCode.ORDER_NOT_OWNER_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void selectProviderRejectsNonBountyOrder() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 5));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderService.selectProvider(2002L, 1002L));

        assertEquals(BaseErrorCode.ORDER_NOT_OPEN_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void selectProviderRejectsNonexistentApplication() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));
        when(orderApplicationDao.selectByOrderIdAndProviderId(2002L, 1002L)).thenReturn(null);

        ClientException exception = assertThrows(ClientException.class,
                () -> orderService.selectProvider(2002L, 1002L));

        assertEquals(BaseErrorCode.ORDER_APPLICATION_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void payOrderUpdatesStatusToPendingFulfill() {
        OrderDO order = buildOrder(2002L, 1001L, 2);
        when(orderDao.selectById(2002L)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.payOrder(2002L));

        verify(orderSettlementService).markPaidAndCreateEscrow(2002L);
    }

    @Test
    void payOrderRejectsNonOwner() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1003L, 2));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderService.payOrder(2002L));

        assertEquals(BaseErrorCode.ORDER_NOT_OWNER_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void payOrderRejectsNonPendingPayStatus() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderService.payOrder(2002L));

        assertEquals(BaseErrorCode.ORDER_PAYMENT_ERROR.code(), exception.getErrorCode());
    }

    private OrderDO buildOrder(Long orderId, Long ownerId, Integer status) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(ownerId);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(98));
        order.setServiceDate(LocalDate.of(2026, 5, 3));
        order.setServiceStartTime(LocalTime.of(14, 0));
        order.setServiceEndTime(LocalTime.of(15, 0));
        order.setServiceType(1);
        order.setAddressSnapshotId(22001L);
        order.setCreatedAt(LocalDateTime.of(2026, 5, 1, 9, 30, 0));
        return order;
    }

    private OrderPetSnapshotDO buildSnapshot(Long snapshotId, Long orderId, Long archivePetId, String snapPetName) {
        OrderPetSnapshotDO snapshot = new OrderPetSnapshotDO();
        snapshot.setSnapshotId(snapshotId);
        snapshot.setOrderId(orderId);
        snapshot.setArchivePetId(archivePetId);
        snapshot.setSnapPetName(snapPetName);
        snapshot.setSnapPetType(1);
        return snapshot;
    }

    private PetArchiveDO buildPetArchive(Long petId, String petName, Integer petType) {
        PetArchiveDO petArchive = new PetArchiveDO();
        petArchive.setPetId(petId);
        petArchive.setPetName(petName);
        petArchive.setPetType(petType);
        return petArchive;
    }

    private UserAddressDO buildAddress(Long addressId, Long userId, String city) {
        UserAddressDO address = new UserAddressDO();
        address.setAddressId(addressId);
        address.setUserId(userId);
        address.setProvince(city);
        address.setCity(city);
        address.setDistrict(city);
        address.setDetailAddress("测试地址");
        address.setContactName("小林");
        address.setContactPhone("13800000001");
        return address;
    }

    private OrderApplicationDO buildApplication(Long applyId, Long orderId, Long providerId, Integer applyStatus) {
        OrderApplicationDO application = new OrderApplicationDO();
        application.setApplyId(applyId);
        application.setOrderId(orderId);
        application.setProviderId(providerId);
        application.setApplyStatus(applyStatus);
        return application;
    }

    private UserDO buildUser(Long userId, String nickname, String avatarUrl) {
        UserDO user = new UserDO();
        user.setUserId(userId);
        user.setNickname(nickname);
        user.setAvatarUrl(avatarUrl);
        return user;
    }
}
