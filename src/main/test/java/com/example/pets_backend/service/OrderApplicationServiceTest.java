package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dto.resp.OrderApplicationRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OrderHardFilterService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderApplicationDao orderApplicationDao;

    @Mock
    private OrderAddressSnapshotDao orderAddressSnapshotDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private CaretakerDepositService caretakerDepositService;

    @Mock
    private OrderHardFilterService orderHardFilterService;

    private OrderApplicationService orderApplicationService;

    @BeforeEach
    void setUp() {
        orderApplicationService = new OrderApplicationService(
                orderDao,
                orderApplicationDao,
                orderAddressSnapshotDao,
                sitterProfileDao,
                caretakerDepositService,
                orderHardFilterService);
        UserContext.setUser(new UserInfoDTO(1002L, "13800000002", "阿周", 2, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void applyCreatesApplicationSuccessfully() {
        OrderDO order = buildOrder(2002L, 1001L, 1);
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(orderApplicationDao.existsByOrderIdAndProviderId(2002L, 1002L)).thenReturn(false);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 2));
        when(orderAddressSnapshotDao.selectById(22001L)).thenReturn(new OrderAddressSnapshotDO());
        doAnswer(invocation -> {
            OrderApplicationDO app = invocation.getArgument(0);
            app.setApplyId(4004L);
            return null;
        }).when(orderApplicationDao).insert(any());

        OrderApplicationRespDTO resp = orderApplicationService.apply(2002L);

        assertEquals(4004L, resp.applicationId());
        verify(orderApplicationDao).insert(any());
    }

    @Test
    void applyRejectsNonCaretakerRole() {
        UserContext.clear();
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(2002L));

        assertEquals(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyRejectsNonExistentOrder() {
        when(orderDao.selectById(9999L)).thenReturn(null);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 2));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(9999L));

        assertEquals(BaseErrorCode.ORDER_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyRejectsNonBountyOrder() {
        when(orderDao.selectById(2001L)).thenReturn(buildOrder(2001L, 1001L, 5));
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 2));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(2001L));

        assertEquals(BaseErrorCode.ORDER_NOT_OPEN_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyRejectsSelfOwnedOrder() {
        UserContext.clear();
        UserContext.setUser(new UserInfoDTO(1002L, "13800000002", "双身份用户", 3, "jwt-token"));
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1002L, 1));
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 2));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(2002L));

        assertEquals(BaseErrorCode.ORDER_OWNER_PROVIDER_CONFLICT_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyRejectsDuplicateApplication() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));
        when(orderApplicationDao.existsByOrderIdAndProviderId(2002L, 1002L)).thenReturn(true);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 2));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(2002L));

        assertEquals(BaseErrorCode.ORDER_ALREADY_APPLIED_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyRejectsMissingUserContext() {
        UserContext.clear();

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(2002L));

        assertEquals(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyRejectsHardFilterMismatch() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));
        when(orderApplicationDao.existsByOrderIdAndProviderId(2002L, 1002L)).thenReturn(false);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 2));
        when(orderAddressSnapshotDao.selectById(22001L)).thenReturn(new OrderAddressSnapshotDO());
        doThrow(new ClientException(BaseErrorCode.ORDER_HARD_FILTER_NOT_MATCH_ERROR))
                .when(orderHardFilterService)
                .ensureProviderEligible(any(), any(), any());

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.apply(2002L));

        assertEquals(BaseErrorCode.ORDER_HARD_FILTER_NOT_MATCH_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void applyAllowsBothRoleOnOtherUsersOrder() {
        UserContext.clear();
        UserContext.setUser(new UserInfoDTO(1003L, "13800000003", "小陈", 3, "jwt-token"));

        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));
        when(orderApplicationDao.existsByOrderIdAndProviderId(2002L, 1003L)).thenReturn(false);
        when(sitterProfileDao.selectById(1003L)).thenReturn(buildProfile(1003L, 2));
        when(orderAddressSnapshotDao.selectById(22001L)).thenReturn(new OrderAddressSnapshotDO());
        doAnswer(invocation -> {
            OrderApplicationDO app = invocation.getArgument(0);
            app.setApplyId(4005L);
            return null;
        }).when(orderApplicationDao).insert(any());

        OrderApplicationRespDTO resp = orderApplicationService.apply(2002L);

        assertEquals(4005L, resp.applicationId());
    }

    @Test
    void cancelDeletesApplicationSuccessfully() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));
        when(orderApplicationDao.deleteByOrderIdAndProviderId(2002L, 1002L)).thenReturn(1);

        assertDoesNotThrow(() -> orderApplicationService.cancel(2002L));
        verify(orderApplicationDao).deleteByOrderIdAndProviderId(2002L, 1002L);
    }

    @Test
    void cancelRejectsWhenNoApplicationFound() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder(2002L, 1001L, 1));
        when(orderApplicationDao.deleteByOrderIdAndProviderId(2002L, 1002L)).thenReturn(0);

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.cancel(2002L));

        assertEquals(BaseErrorCode.ORDER_APPLICATION_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void cancelRejectsNonBountyOrder() {
        when(orderDao.selectById(2001L)).thenReturn(buildOrder(2001L, 1001L, 5));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.cancel(2001L));

        assertEquals(BaseErrorCode.ORDER_NOT_OPEN_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void cancelRejectsNonCaretakerRole() {
        UserContext.clear();
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));

        ClientException exception = assertThrows(ClientException.class,
                () -> orderApplicationService.cancel(2002L));

        assertEquals(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR.code(), exception.getErrorCode());
    }

    private SitterProfileDO buildProfile(Long providerId, Integer verifyStatus) {
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(providerId);
        profile.setVerifyStatus(verifyStatus);
        return profile;
    }

    private OrderDO buildOrder(Long orderId, Long ownerId, Integer status) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(ownerId);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(98));
        order.setServiceDate(LocalDate.of(2026, 5, 3));
        order.setAddressSnapshotId(22001L);
        return order;
    }
}
