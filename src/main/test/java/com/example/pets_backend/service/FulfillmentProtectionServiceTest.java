package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.config.FulfillmentProtectionProperties;
import com.example.pets_backend.dao.ExceptionReportDao;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.ExceptionReportDO;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.req.SelfReportExceptionReqDTO;
import com.example.pets_backend.enums.EmergencyExceptionTypeEnum;
import com.example.pets_backend.enums.ExceptionReportStatusEnum;
import com.example.pets_backend.enums.OrderStatusEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.official.OfficialMessageService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FulfillmentProtectionServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private FulfillmentRecordDao fulfillmentRecordDao;

    @Mock
    private ExceptionReportDao exceptionReportDao;

    @Mock
    private OfficialMessageService officialMessageService;

    @Mock
    private OrderSettlementService orderSettlementService;

    private FulfillmentProtectionService fulfillmentProtectionService;

    @BeforeEach
    void setUp() {
        FulfillmentProtectionProperties properties = new FulfillmentProtectionProperties();
        properties.setArrivalTimeoutMinutes(30);
        properties.setInactivityTimeoutMinutes(30);
        properties.setOwnerResponseTimeoutMinutes(30);
        fulfillmentProtectionService = new FulfillmentProtectionService(orderDao, fulfillmentRecordDao,
                exceptionReportDao, properties, officialMessageService, orderSettlementService);
        UserContext.setUser(new UserInfoDTO(1002L, "13800000002", "履约官", 2, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void scanAndProtectOrdersBlocksPossibleLateOrder() {
        OrderDO order = buildOrder(2003L, 1001L, 1002L, OrderStatusEnum.PENDING_FULFILLMENT.getCode());
        order.setCreatedAt(LocalDateTime.now().minusMinutes(45));
        when(orderDao.selectByStatus(OrderStatusEnum.PENDING_FULFILLMENT.getCode())).thenReturn(List.of(order));
        when(orderDao.selectByStatus(OrderStatusEnum.IN_FULFILLMENT.getCode())).thenReturn(List.of());
        when(fulfillmentRecordDao.existsByOrderIdAndNodeType(2003L, 1)).thenReturn(false);

        assertDoesNotThrow(() -> fulfillmentProtectionService.scanAndProtectOrders());

        verify(orderDao).updateStatus(2003L, OrderStatusEnum.BLOCKED_WAIT_OWNER.getCode());
        verify(fulfillmentRecordDao).insert(any(FulfillmentRecordDO.class));
        verify(officialMessageService).sendSystemOfficialMessage(eq(2003L), eq(1001L),
                eq("【系统通知】订单2003可能迟到，系统预警: 超时未到达。当前状态已变更为履约受阻-等待雇主确认，请尽快处理。"));
    }

    @Test
    void ownerConfirmResolvedRestoresInServiceStatus() {
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "宠主", 1, "jwt-token"));
        OrderDO order = buildOrder(2002L, 1001L, 1002L, OrderStatusEnum.BLOCKED_WAIT_OWNER.getCode());
        when(orderDao.selectById(2002L)).thenReturn(order);

        assertDoesNotThrow(() -> fulfillmentProtectionService.ownerConfirmResolved(2002L));

        verify(orderDao).updateStatus(2002L, OrderStatusEnum.IN_FULFILLMENT.getCode());
        verify(fulfillmentRecordDao).insert(any(FulfillmentRecordDO.class));
    }

    @Test
    void noFaultRetreatRejectsWhenOwnerResponseWindowNotExpired() {
        OrderDO order = buildOrder(2005L, 1001L, 1002L, OrderStatusEnum.BLOCKED_WAIT_OWNER.getCode());
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
        when(orderDao.selectById(2005L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> fulfillmentProtectionService.noFaultRetreat(2005L));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
        verify(orderDao, never()).updateStatus(any(), any());
        verify(orderSettlementService, never()).settleExceptionEnded(any());
    }

    @Test
    void noFaultRetreatSettlesFundsAndUpdatesToExceptionEndedAfterTimeout() {
        OrderDO order = buildOrder(2006L, 1001L, 1002L, OrderStatusEnum.BLOCKED_WAIT_OWNER.getCode());
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(31));
        when(orderDao.selectById(2006L)).thenReturn(order);

        assertDoesNotThrow(() -> fulfillmentProtectionService.noFaultRetreat(2006L));

        verify(orderSettlementService).settleExceptionEnded(2006L);
        verify(orderDao).updateStatus(2006L, OrderStatusEnum.EXCEPTION_ENDED.getCode());
        verify(fulfillmentRecordDao).insert(any(FulfillmentRecordDO.class));
    }

    @Test
    void selfReportExceptionMovesToEmergencyPlatformInterventionForPetAnomaly() {
        OrderDO order = buildOrder(2008L, 1001L, 1002L, OrderStatusEnum.IN_FULFILLMENT.getCode());
        when(orderDao.selectById(2008L)).thenReturn(order);

        assertDoesNotThrow(() -> fulfillmentProtectionService
                .selfReportException(2008L, new SelfReportExceptionReqDTO(
                        EmergencyExceptionTypeEnum.PET_ANOMALY.getCode(), "宠物奄奄一息")));

        verify(orderDao).updateStatus(2008L, OrderStatusEnum.EMERGENCY_PLATFORM_INTERVENTION.getCode());
        verify(fulfillmentRecordDao).insert(any(FulfillmentRecordDO.class));

        ArgumentCaptor<ExceptionReportDO> reportCaptor = ArgumentCaptor.forClass(ExceptionReportDO.class);
        verify(exceptionReportDao).insert(reportCaptor.capture());
        assertEquals(2008L, reportCaptor.getValue().getOrderId());
        assertEquals(1002L, reportCaptor.getValue().getReporterId());
        assertEquals(EmergencyExceptionTypeEnum.PET_ANOMALY.getCode(), reportCaptor.getValue().getExceptionType());
        assertEquals(ExceptionReportStatusEnum.PENDING.getCode(), reportCaptor.getValue().getReportStatus());

        verify(officialMessageService).sendSystemOfficialMessage(eq(2008L), eq(1001L),
                eq("【系统通知】订单2008紧急求助-宠物异常，紧急求助 宠物异常，desc=宠物奄奄一息。当前状态已变更为紧急终止/平台介入，请尽快处理。"));
    }

    @Test
    void selfReportExceptionSupportsPersonalThreatType() {
        OrderDO order = buildOrder(2009L, 1001L, 1002L, OrderStatusEnum.IN_FULFILLMENT.getCode());
        when(orderDao.selectById(2009L)).thenReturn(order);

        assertDoesNotThrow(() -> fulfillmentProtectionService
                .selfReportException(2009L, new SelfReportExceptionReqDTO(
                        EmergencyExceptionTypeEnum.PERSONAL_THREAT.getCode(), "宠物具有极强攻击性")));

        ArgumentCaptor<ExceptionReportDO> reportCaptor = ArgumentCaptor.forClass(ExceptionReportDO.class);
        verify(exceptionReportDao).insert(reportCaptor.capture());
        assertEquals(EmergencyExceptionTypeEnum.PERSONAL_THREAT.getCode(), reportCaptor.getValue().getExceptionType());
        verify(orderDao).updateStatus(2009L, OrderStatusEnum.EMERGENCY_PLATFORM_INTERVENTION.getCode());
    }

    @Test
    void selfReportExceptionRejectsWhenNotInService() {
        OrderDO order = buildOrder(2010L, 1001L, 1002L, OrderStatusEnum.PENDING_FULFILLMENT.getCode());
        when(orderDao.selectById(2010L)).thenReturn(order);

        assertThrows(ClientException.class, () -> fulfillmentProtectionService
                .selfReportException(2010L, new SelfReportExceptionReqDTO(
                        EmergencyExceptionTypeEnum.PET_ANOMALY.getCode(), "宠物异常")));

        verify(orderDao, never()).updateStatus(any(), any());
        verify(exceptionReportDao, never()).insert(any());
    }

    @Test
    void selfReportExceptionRejectsUnsupportedExceptionType() {
        OrderDO order = buildOrder(2011L, 1001L, 1002L, OrderStatusEnum.IN_FULFILLMENT.getCode());
        when(orderDao.selectById(2011L)).thenReturn(order);

        assertThrows(ClientException.class, () -> fulfillmentProtectionService
                .selfReportException(2011L, new SelfReportExceptionReqDTO(1, "门禁异常")));

        verify(orderDao, never()).updateStatus(any(), any());
        verify(exceptionReportDao, never()).insert(any());
    }

    private OrderDO buildOrder(Long orderId, Long ownerId, Long providerId, Integer status) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(ownerId);
        order.setProviderId(providerId);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(100));
        return order;
    }

}
