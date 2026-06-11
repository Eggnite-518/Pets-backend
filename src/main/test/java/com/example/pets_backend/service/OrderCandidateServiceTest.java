package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.example.pets_backend.dto.resp.CandidateListRespDTO;
import com.example.pets_backend.dto.resp.ProviderDetailRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OrderHardFilterService;
import com.example.pets_backend.service.support.ProviderProfileSupportService;
import com.example.pets_backend.service.support.ProviderPublicMetrics;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCandidateServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderApplicationDao orderApplicationDao;

    @Mock
    private OrderAddressSnapshotDao orderAddressSnapshotDao;

    @Mock
    private OrderPetSnapshotDao orderPetSnapshotDao;

    @Mock
    private UserDao userDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private ProviderProfileSupportService providerProfileSupportService;

    @Mock
    private OrderHardFilterService orderHardFilterService;

    private OrderCandidateService orderCandidateService;

    @BeforeEach
    void setUp() {
        orderCandidateService = new OrderCandidateService(orderDao, orderApplicationDao, orderAddressSnapshotDao,
                orderPetSnapshotDao, userDao, sitterProfileDao, providerProfileSupportService, orderHardFilterService);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void listCandidatesSortsByDistance() {
        OrderDO order = buildOrder(2002L, 1001L, 7001L);
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderApplicationDO nearApplication = buildApplication(4001L, 2002L, 1002L, 0);
        OrderApplicationDO farApplication = buildApplication(4002L, 2002L, 1003L, 0);
        when(orderApplicationDao.selectApplyingByOrderId(2002L)).thenReturn(List.of(farApplication, nearApplication));

        UserDO nearUser = buildUser(1002L, "近邻", "https://example.com/near.png");
        UserDO farUser = buildUser(1003L, "远邻", "https://example.com/far.png");
        when(userDao.selectByIds(any())).thenReturn(List.of(nearUser, farUser));

        SitterProfileDO nearProfile = buildProfile(1002L, 31.2304, 121.4737);
        SitterProfileDO farProfile = buildProfile(1003L, 31.3000, 121.6000);
        when(sitterProfileDao.selectByIds(any())).thenReturn(List.of(nearProfile, farProfile));

        OrderAddressSnapshotDO addressSnapshot = new OrderAddressSnapshotDO();
        addressSnapshot.setSnapshotId(7001L);
        addressSnapshot.setLatitude(31.2300);
        addressSnapshot.setLongitude(121.4700);
        when(orderAddressSnapshotDao.selectById(7001L)).thenReturn(addressSnapshot);

        when(providerProfileSupportService.resolveMetrics(1002L))
                .thenReturn(buildMetrics(90, 4.8, 20, 98.0));
        when(providerProfileSupportService.resolveMetrics(1003L))
                .thenReturn(buildMetrics(85, 4.9, 30, 96.0));
        when(providerProfileSupportService.resolveDistanceKm(addressSnapshot, nearProfile)).thenReturn(0.5);
        when(providerProfileSupportService.resolveDistanceKm(addressSnapshot, farProfile)).thenReturn(12.3);
        when(orderHardFilterService.isProviderEligible(nearProfile, addressSnapshot, order)).thenReturn(true);
        when(orderHardFilterService.isProviderEligible(farProfile, addressSnapshot, order)).thenReturn(true);

        CandidateListRespDTO result = orderCandidateService.listCandidates(2002L, "distance");

        assertEquals("distance", result.sortBy());
        assertEquals(2, result.candidates().size());
        assertEquals(1002L, result.candidates().get(0).providerId());
        assertEquals(0.5, result.candidates().get(0).distanceKm());
        assertEquals(1003L, result.candidates().get(1).providerId());
    }

    @Test
    void listCandidatesSortsByRating() {
        OrderDO order = buildOrder(2002L, 1001L, 7001L);
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderApplicationDO lowRating = buildApplication(4001L, 2002L, 1002L, 0);
        OrderApplicationDO highRating = buildApplication(4002L, 2002L, 1003L, 0);
        when(orderApplicationDao.selectApplyingByOrderId(2002L)).thenReturn(List.of(lowRating, highRating));
        when(userDao.selectByIds(any())).thenReturn(List.of(
                buildUser(1002L, "A", null),
                buildUser(1003L, "B", null)));
        when(sitterProfileDao.selectByIds(any())).thenReturn(List.of(
                buildProfile(1002L, 31.2, 121.4),
                buildProfile(1003L, 31.2, 121.4)));
        when(orderAddressSnapshotDao.selectById(7001L)).thenReturn(new OrderAddressSnapshotDO());
        when(providerProfileSupportService.resolveMetrics(1002L))
                .thenReturn(buildMetrics(80, 4.2, 10, 100.0));
        when(providerProfileSupportService.resolveMetrics(1003L))
                .thenReturn(buildMetrics(95, 4.9, 50, 100.0));
        when(providerProfileSupportService.resolveDistanceKm(any(), any())).thenReturn(1.0);
        when(orderHardFilterService.isProviderEligible(any(), any(), any())).thenReturn(true);

        CandidateListRespDTO result = orderCandidateService.listCandidates(2002L, "rating");

        assertEquals("rating", result.sortBy());
        assertEquals(1003L, result.candidates().get(0).providerId());
        assertEquals(4.9, result.candidates().get(0).rating());
    }

    @Test
    void listCandidatesFiltersOutHardFilterMismatchedProvider() {
        OrderDO order = buildOrder(2002L, 1001L, 7001L);
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderApplicationDO eligibleApplication = buildApplication(4001L, 2002L, 1002L, 0);
        OrderApplicationDO ineligibleApplication = buildApplication(4002L, 2002L, 1003L, 0);
        when(orderApplicationDao.selectApplyingByOrderId(2002L))
                .thenReturn(List.of(eligibleApplication, ineligibleApplication));
        when(userDao.selectByIds(any())).thenReturn(List.of(
                buildUser(1002L, "可选", null),
                buildUser(1003L, "不符合", null)));

        SitterProfileDO eligibleProfile = buildProfile(1002L, 31.2304, 121.4737);
        SitterProfileDO ineligibleProfile = buildProfile(1003L, 31.2305, 121.4738);
        when(sitterProfileDao.selectByIds(any())).thenReturn(List.of(eligibleProfile, ineligibleProfile));

        OrderAddressSnapshotDO addressSnapshot = new OrderAddressSnapshotDO();
        addressSnapshot.setSnapshotId(7001L);
        when(orderAddressSnapshotDao.selectById(7001L)).thenReturn(addressSnapshot);
        when(orderHardFilterService.isProviderEligible(eligibleProfile, addressSnapshot, order)).thenReturn(true);
        when(orderHardFilterService.isProviderEligible(ineligibleProfile, addressSnapshot, order)).thenReturn(false);
        when(providerProfileSupportService.resolveMetrics(1002L)).thenReturn(buildMetrics(88, 4.6, 18, 97.0));
        when(providerProfileSupportService.resolveDistanceKm(addressSnapshot, eligibleProfile)).thenReturn(0.8);

        CandidateListRespDTO result = orderCandidateService.listCandidates(2002L, "distance");

        assertEquals(1, result.candidates().size());
        assertEquals(1002L, result.candidates().get(0).providerId());
    }

    @Test
    void getProviderDetailReturnsQualificationProfile() {
        OrderDO order = buildOrder(2002L, 1001L, 7001L);
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderApplicationDO application = buildApplication(4002L, 2002L, 1002L, 0);
        when(orderApplicationDao.selectByOrderIdAndProviderId(2002L, 1002L)).thenReturn(application);
        when(userDao.selectById(1002L)).thenReturn(buildUser(1002L, "阿周", "https://example.com/avatar.png"));

        SitterProfileDO profile = buildProfile(1002L, 31.2304, 121.4737);
        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);

        OrderAddressSnapshotDO addressSnapshot = new OrderAddressSnapshotDO();
        addressSnapshot.setLatitude(31.2300);
        addressSnapshot.setLongitude(121.4700);
        when(orderAddressSnapshotDao.selectById(7001L)).thenReturn(addressSnapshot);

        OrderPetSnapshotDO snapshot = new OrderPetSnapshotDO();
        snapshot.setSnapPetName("团团");
        snapshot.setSnapPetType(1);
        snapshot.setSnapReq("请自带鞋套");
        when(orderPetSnapshotDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of(snapshot));

        when(providerProfileSupportService.resolveMetrics(1002L))
                .thenReturn(new ProviderPublicMetrics(92, 4.8, 15, 28, 96.5, "银牌宠托师",
                        List.of("实名认证", "10+次服务")));
        when(providerProfileSupportService.resolveDistanceKm(addressSnapshot, profile)).thenReturn(0.6);

        ProviderDetailRespDTO result = orderCandidateService.getProviderDetail(2002L, 1002L);

        assertEquals(1002L, result.providerId());
        assertEquals("阿周", result.providerNickname());
        assertEquals(92, result.creditScore());
        assertEquals(4.8, result.rating());
        assertEquals(28, result.totalOrderCount());
        assertEquals(96.5, result.complianceRate());
        assertEquals("银牌宠托师", result.levelTag());
        assertEquals(2, result.certLabels().size());
        assertEquals(0.6, result.distanceKm());
        assertEquals("团团", result.petName());
    }

    @Test
    void getProviderDetailRejectsNonOwner() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1003L, 7001L));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderCandidateService.getProviderDetail(2002L, 1002L));

        assertEquals(BaseErrorCode.ORDER_NOT_OWNER_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void getProviderDetailWorksForNonBountyOrder() {
        OrderDO order = buildOrder(2002L, 1001L, 7001L);
        order.setStatus(5);
        when(orderDao.selectById(2002L)).thenReturn(order);

        OrderApplicationDO application = buildApplication(4002L, 2002L, 1002L, 2);
        when(orderApplicationDao.selectByOrderIdAndProviderId(2002L, 1002L)).thenReturn(application);
        when(userDao.selectById(1002L)).thenReturn(buildUser(1002L, "阿周", "https://example.com/avatar.png"));

        SitterProfileDO profile = buildProfile(1002L, 31.2304, 121.4737);
        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);

        OrderAddressSnapshotDO addressSnapshot = new OrderAddressSnapshotDO();
        addressSnapshot.setLatitude(31.2300);
        addressSnapshot.setLongitude(121.4700);
        when(orderAddressSnapshotDao.selectById(7001L)).thenReturn(addressSnapshot);
        when(orderPetSnapshotDao.selectByOrderIds(List.of(2002L))).thenReturn(List.of());

        when(providerProfileSupportService.resolveMetrics(1002L))
                .thenReturn(new ProviderPublicMetrics(92, 4.8, 15, 28, 96.5, "银牌宠托师",
                        List.of("实名认证")));
        when(providerProfileSupportService.resolveDistanceKm(addressSnapshot, profile)).thenReturn(0.6);

        ProviderDetailRespDTO result = orderCandidateService.getProviderDetail(2002L, 1002L);

        assertEquals(1002L, result.providerId());
        assertEquals("阿周", result.providerNickname());
    }

    private OrderDO buildOrder(Long orderId, Long ownerId, Long addressSnapshotId) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(ownerId);
        order.setAddressSnapshotId(addressSnapshotId);
        order.setStatus(1);
        order.setTotalAmount(BigDecimal.valueOf(98));
        return order;
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

    private SitterProfileDO buildProfile(Long providerId, double lat, double lng) {
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(providerId);
        profile.setResidentLatitude(BigDecimal.valueOf(lat));
        profile.setResidentLongitude(BigDecimal.valueOf(lng));
        return profile;
    }

    private ProviderPublicMetrics buildMetrics(int creditScore, double rating, int totalOrders, double complianceRate) {
        return new ProviderPublicMetrics(creditScore, rating, 10, totalOrders, complianceRate, "银牌宠托师",
                List.of("实名认证"));
    }
}
