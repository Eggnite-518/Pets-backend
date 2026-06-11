package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.example.pets_backend.config.OfficialMessageProperties;
import com.example.pets_backend.dao.ChatDao;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.ChatDO;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.resp.EvidenceChainRespDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenceChainServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private FulfillmentRecordDao fulfillmentRecordDao;

    @Mock
    private ChatDao chatDao;

    @Mock
    private FulfillmentMediaStorageService fulfillmentMediaStorageService;

    private EvidenceChainService evidenceChainService;

    @BeforeEach
    void setUp() {
        OfficialMessageProperties properties = new OfficialMessageProperties();
        properties.setSystemSenderId(999999L);
        evidenceChainService = new EvidenceChainService(orderDao, fulfillmentRecordDao, chatDao,
                properties, fulfillmentMediaStorageService);
    }

    @Test
    void getEvidenceChainAggregatesOrderRecordsAndOfficialMessages() {
        OrderDO order = new OrderDO();
        order.setOrderId(2002L);
        order.setOwnerId(1001L);
        order.setProviderId(1002L);
        order.setStatus(6);
        when(orderDao.selectById(2002L)).thenReturn(order);

        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setOrderId(2002L);
        record.setNodeType(1);
        record.setMediaType("IMAGE");
        record.setObjectKey("fulfillment/orders/2002/nodes/1/a.jpg");
        record.setCreatedAt(LocalDateTime.of(2026, 6, 2, 10, 0, 0));
        when(fulfillmentRecordDao.selectByOrderId(2002L)).thenReturn(List.of(record));
        when(fulfillmentMediaStorageService.resolveAccessibleUrl(record))
                .thenReturn("https://signed.example/a.jpg");

        ChatDO chat = new ChatDO();
        chat.setMessageId(3001L);
        chat.setOrderId(2002L);
        chat.setSenderId(999999L);
        chat.setReceiverId(1001L);
        chat.setContent("异常提醒");
        chat.setCreatedAt(LocalDateTime.of(2026, 6, 2, 10, 1, 0));
        when(chatDao.selectOfficialByOrderId(2002L, 999999L)).thenReturn(List.of(chat));

        EvidenceChainRespDTO result = evidenceChainService.getEvidenceChain(2002L);

        assertEquals(2002L, result.order().orderId());
        assertEquals(6, result.order().orderStatus());
        assertEquals("https://signed.example/a.jpg", result.fulfillmentRecords().get(0).imageUrl());
        assertEquals("异常提醒", result.officialMessages().get(0).content());
    }
}
