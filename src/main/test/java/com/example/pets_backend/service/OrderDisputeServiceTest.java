package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.ArbitrationRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.ArbitrationRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.req.SubmitDisputeReqDTO;
import com.example.pets_backend.dto.resp.DisputeRespDTO;
import com.example.pets_backend.dto.resp.SubmitDisputeRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
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
class OrderDisputeServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private ArbitrationRecordDao arbitrationRecordDao;

    @Mock
    private EvidenceChainService evidenceChainService;

    private OrderDisputeService orderDisputeService;

    @BeforeEach
    void setUp() {
        orderDisputeService = new OrderDisputeService(orderDao, arbitrationRecordDao, evidenceChainService);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "owner", 1, "token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void submitDisputeCreatesPendingArbitrationRecord() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder());

        SubmitDisputeRespDTO result = orderDisputeService.submitDispute(2002L,
                new SubmitDisputeReqDTO(1, " 服务质量争议 ", List.of("https://example.com/a.jpg")));

        ArgumentCaptor<ArbitrationRecordDO> captor = ArgumentCaptor.forClass(ArbitrationRecordDO.class);
        verify(arbitrationRecordDao).insert(captor.capture());
        ArbitrationRecordDO record = captor.getValue();
        assertEquals(1001L, record.getPlaintiffId());
        assertEquals(1002L, record.getDefendantId());
        assertEquals(1, record.getArbType());
        assertEquals("服务质量争议", record.getReason());
        assertEquals("https://example.com/a.jpg", record.getEvidenceUrls());
        assertEquals(0, result.disputeStatus());
    }

    @Test
    void submitDisputeRejectsDuplicatedActiveDispute() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder());
        when(arbitrationRecordDao.existsActiveByOrderIdAndPlaintiffId(2002L, 1001L)).thenReturn(true);

        ClientException exception = assertThrows(ClientException.class,
                () -> orderDisputeService.submitDispute(2002L,
                        new SubmitDisputeReqDTO(1, "服务质量争议", List.of())));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
        verify(arbitrationRecordDao, never()).insert(any());
    }

    @Test
    void submitDisputeRejectsUnrelatedUser() {
        UserContext.setUser(new UserInfoDTO(1003L, "13800000003", "other", 1, "token"));
        when(orderDao.selectById(2002L)).thenReturn(buildOrder());

        ClientException exception = assertThrows(ClientException.class,
                () -> orderDisputeService.submitDispute(2002L,
                        new SubmitDisputeReqDTO(1, "服务质量争议", List.of())));

        assertEquals(BaseErrorCode.ORDER_NOT_OWNER_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void submitDisputeRejectsOrderBeforePayment() {
        OrderDO order = buildOrder();
        order.setStatus(2);
        when(orderDao.selectById(2002L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> orderDisputeService.submitDispute(2002L,
                        new SubmitDisputeReqDTO(1, "服务质量争议", List.of())));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
        verify(arbitrationRecordDao, never()).insert(any());
    }

    @Test
    void listDisputesRejectsOrderBeforePayment() {
        OrderDO order = buildOrder();
        order.setStatus(2);
        when(orderDao.selectById(2002L)).thenReturn(order);

        ClientException exception = assertThrows(ClientException.class,
                () -> orderDisputeService.listDisputes(2002L));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
        verify(arbitrationRecordDao, never()).selectByOrderId(any());
    }

    @Test
    void listDisputesReturnsOrderArbitrationRecords() {
        when(orderDao.selectById(2002L)).thenReturn(buildOrder());
        ArbitrationRecordDO record = new ArbitrationRecordDO();
        record.setArbitrationId(7001L);
        record.setOrderId(2002L);
        record.setPlaintiffId(1001L);
        record.setDefendantId(1002L);
        record.setArbType(1);
        record.setReason("服务质量争议");
        record.setEvidenceUrls("https://example.com/a.jpg,https://example.com/b.jpg");
        record.setArbitrationStatus(0);
        record.setCreatedAt(LocalDateTime.of(2026, 6, 2, 10, 0, 0));
        when(arbitrationRecordDao.selectByOrderId(2002L)).thenReturn(List.of(record));

        List<DisputeRespDTO> result = orderDisputeService.listDisputes(2002L);

        assertEquals(7001L, result.get(0).disputeId());
        assertEquals(2, result.get(0).evidenceUrls().size());
        assertEquals("2026-06-02 10:00:00", result.get(0).createdAt());
    }

    private OrderDO buildOrder() {
        OrderDO order = new OrderDO();
        order.setOrderId(2002L);
        order.setOwnerId(1001L);
        order.setProviderId(1002L);
        order.setStatus(5);
        return order;
    }
}
