package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.FinancialLogDao;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderSettlementDao;
import com.example.pets_backend.dao.PlatformFinancialLogDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.FinancialLogDO;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderSettlementDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderSettlementServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private FulfillmentRecordDao fulfillmentRecordDao;

    @Mock
    private OrderSettlementDao orderSettlementDao;

    @Mock
    private UserDao userDao;

    @Mock
    private FinancialLogDao financialLogDao;

    @Mock
    private PlatformFinancialLogDao platformFinancialLogDao;

    private OrderSettlementService orderSettlementService;

    @BeforeEach
    void setUp() {
        orderSettlementService = new OrderSettlementService(orderDao, fulfillmentRecordDao, orderSettlementDao, userDao,
                financialLogDao, platformFinancialLogDao);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "owner", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void ownerConfirmCompletionSettlesProviderIncomeAndPlatformCommission() {
        OrderDO order = new OrderDO();
        order.setOrderId(2002L);
        order.setOwnerId(1001L);
        order.setProviderId(1002L);
        order.setStatus(5);
        order.setServiceType(1);
        order.setTotalAmount(new BigDecimal("100.00"));
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(fulfillmentRecordDao.selectByOrderId(2002L)).thenReturn(completeFeedingFulfillmentRecords(2002L));

        OrderSettlementDO settlement = new OrderSettlementDO();
        settlement.setSettlementId(15002L);
        settlement.setOrderId(2002L);
        settlement.setOwnerId(1001L);
        settlement.setProviderId(1002L);
        settlement.setGrossAmount(new BigDecimal("100.00"));
        settlement.setCommissionRate(new BigDecimal("0.30"));
        settlement.setCommissionAmount(new BigDecimal("30.00"));
        settlement.setProviderIncome(new BigDecimal("70.00"));
        settlement.setSettlementStatus(1);
        when(orderSettlementDao.selectByOrderId(2002L)).thenReturn(settlement);

        UserDO provider = new UserDO();
        provider.setUserId(1002L);
        provider.setBalance(new BigDecimal("20.00"));
        when(userDao.selectById(1002L)).thenReturn(provider);

        orderSettlementService.ownerConfirmCompletion(2002L);

        assertEquals(new BigDecimal("90.00"), provider.getBalance());
        verify(userDao).updateById(provider);
        verify(orderDao).updateStatus(2002L, 6);

        ArgumentCaptor<FinancialLogDO> providerLogCaptor = ArgumentCaptor.forClass(FinancialLogDO.class);
        verify(financialLogDao).insert(providerLogCaptor.capture());
        assertEquals(new BigDecimal("70.00"), providerLogCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("90.00"), providerLogCaptor.getValue().getBalanceAfter());
        assertEquals(11, providerLogCaptor.getValue().getTradeType());

        ArgumentCaptor<com.example.pets_backend.dao.entity.PlatformFinancialLogDO> platformLogCaptor =
                ArgumentCaptor.forClass(com.example.pets_backend.dao.entity.PlatformFinancialLogDO.class);
        verify(platformFinancialLogDao).insert(platformLogCaptor.capture());
        assertEquals(new BigDecimal("30.00"), platformLogCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("30.00"), platformLogCaptor.getValue().getBalanceAfter());
        assertEquals(101, platformLogCaptor.getValue().getTradeType());

        assertEquals(2, settlement.getSettlementStatus());
        verify(orderSettlementDao).updateById(settlement);
    }

    private static List<FulfillmentRecordDO> completeFeedingFulfillmentRecords(Long orderId) {
        return List.of(1, 2, 3, 4, 6).stream().map(nodeType -> {
            FulfillmentRecordDO record = new FulfillmentRecordDO();
            record.setOrderId(orderId);
            record.setNodeType(nodeType);
            record.setProcessingStatus("SUCCESS");
            return record;
        }).toList();
    }

    @Test
    void settleExceptionEndedCreditsProviderAndRefundsOwnerToBalance() {
        OrderDO order = new OrderDO();
        order.setOrderId(2006L);
        order.setOwnerId(1001L);
        order.setProviderId(1002L);
        order.setStatus(7);
        order.setTotalAmount(new BigDecimal("100.00"));
        when(orderDao.selectById(2006L)).thenReturn(order);

        OrderSettlementDO settlement = new OrderSettlementDO();
        settlement.setSettlementId(15006L);
        settlement.setOrderId(2006L);
        settlement.setOwnerId(1001L);
        settlement.setProviderId(1002L);
        settlement.setGrossAmount(new BigDecimal("100.00"));
        settlement.setCommissionRate(new BigDecimal("0.30"));
        settlement.setCommissionAmount(new BigDecimal("30.00"));
        settlement.setProviderIncome(new BigDecimal("70.00"));
        settlement.setSettlementStatus(1);
        when(orderSettlementDao.selectByOrderId(2006L)).thenReturn(settlement);

        UserDO provider = new UserDO();
        provider.setUserId(1002L);
        provider.setBalance(new BigDecimal("10.00"));
        UserDO owner = new UserDO();
        owner.setUserId(1001L);
        owner.setBalance(new BigDecimal("5.00"));
        when(userDao.selectById(1002L)).thenReturn(provider);
        when(userDao.selectById(1001L)).thenReturn(owner);

        orderSettlementService.settleExceptionEnded(2006L);

        assertEquals(new BigDecimal("60.00"), provider.getBalance());
        assertEquals(new BigDecimal("55.00"), owner.getBalance());
        verify(userDao).updateById(provider);
        verify(userDao).updateById(owner);

        ArgumentCaptor<FinancialLogDO> logCaptor = ArgumentCaptor.forClass(FinancialLogDO.class);
        verify(financialLogDao, org.mockito.Mockito.times(2)).insert(logCaptor.capture());
        FinancialLogDO providerLog = logCaptor.getAllValues().get(0);
        FinancialLogDO ownerLog = logCaptor.getAllValues().get(1);
        assertEquals(1002L, providerLog.getUserId());
        assertEquals(new BigDecimal("50.00"), providerLog.getAmount());
        assertEquals(12, providerLog.getTradeType());
        assertEquals(1001L, ownerLog.getUserId());
        assertEquals(new BigDecimal("50.00"), ownerLog.getAmount());
        assertEquals(22, ownerLog.getTradeType());

        assertEquals(2, settlement.getSettlementStatus());
        assertEquals(new BigDecimal("50.00"), settlement.getProviderIncome());
        assertEquals(BigDecimal.ZERO, settlement.getCommissionAmount());
        assertEquals(BigDecimal.ZERO, settlement.getCommissionRate());
        verify(orderSettlementDao).updateById(settlement);
    }
}
