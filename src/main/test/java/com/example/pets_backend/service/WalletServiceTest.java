package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.api.AlipayClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pets_backend.config.AlipayProperties;
import com.example.pets_backend.dao.FinancialLogDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.WithdrawalRecordDao;
import com.example.pets_backend.dao.entity.FinancialLogDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.CaretakerWalletWithdrawReqDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletRecordsRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletWithdrawRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private FinancialLogDao financialLogDao;

    @Mock
    private WithdrawalRecordDao withdrawalRecordDao;

    @Mock
    private AlipayClient alipayClient;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(userDao, financialLogDao, withdrawalRecordDao, alipayClient,
                new AlipayProperties(), new ObjectMapper());
        UserContext.setUser(new UserInfoDTO(1002L, "13800000002", "provider", 2, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void applyCaretakerWithdrawFreezesAmountAndReturnsBalance() {
        UserDO user = new UserDO();
        user.setUserId(1002L);
        user.setPhone("13800000002");
        user.setBalance(BigDecimal.valueOf(500));
        user.setFrozenAmount(BigDecimal.valueOf(10));
        when(userDao.selectById(1002L)).thenReturn(user);

        CaretakerWalletWithdrawRespDTO result = walletService.applyCaretakerWithdraw(
                new CaretakerWalletWithdrawReqDTO(BigDecimal.valueOf(200)));

        assertEquals("300.00", result.balance());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(user.getBalance()));
        assertEquals(0, BigDecimal.valueOf(210).compareTo(user.getFrozenAmount()));
        verify(withdrawalRecordDao).insert(any());
        verify(financialLogDao).insert(any());
        verify(userDao).updateById(user);
    }

    @Test
    void listCaretakerWalletRecordsMapsFinancialLogs() {
        FinancialLogDO log = new FinancialLogDO();
        log.setLogId(12002L);
        log.setUserId(1002L);
        log.setAmount(BigDecimal.valueOf(168));
        log.setBalanceAfter(BigDecimal.valueOf(488));
        log.setTradeType(11);
        log.setRelationId(2001L);
        log.setCreatedAt(LocalDateTime.of(2026, 5, 1, 18, 11));
        Page<FinancialLogDO> page = new Page<>(1, 20, 1);
        page.setRecords(List.of(log));
        when(financialLogDao.selectByUserId(1002L, 1, 20)).thenReturn(page);

        CaretakerWalletRecordsRespDTO result = walletService.listCaretakerWalletRecords(1, 20);

        assertEquals(1, result.total());
        assertEquals("12002", result.list().get(0).recordId());
        assertEquals(1, result.list().get(0).direction());
        assertEquals("168.00", result.list().get(0).amount());
    }
}
