package com.example.pets_backend.service;

import com.example.pets_backend.config.OfficialMessageProperties;
import com.example.pets_backend.dao.ChatDao;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.ChatDO;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.resp.EvidenceChainRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordRespDTO;
import com.example.pets_backend.dto.resp.OfficialMessageRespDTO;
import com.example.pets_backend.dto.resp.OrderMetaRespDTO;
import com.example.pets_backend.enums.OrderStatusEnum;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.fulfillment.FulfillmentNodeType;
import com.example.pets_backend.service.support.FulfillmentRecordQuerySupport;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvidenceChainService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderDao orderDao;
    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final ChatDao chatDao;
    private final OfficialMessageProperties officialMessageProperties;
    private final FulfillmentMediaStorageService fulfillmentMediaStorageService;

    public EvidenceChainRespDTO getEvidenceChain(Long orderId) {
        if (orderId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return new EvidenceChainRespDTO(
                new OrderMetaRespDTO(
                        order.getOrderId(),
                        order.getOwnerId(),
                        order.getProviderId(),
                        order.getStatus(),
                        OrderStatusEnum.getDescByCode(order.getStatus())),
                FulfillmentRecordQuerySupport
                        .pickLatestDisplayRecords(fulfillmentRecordDao.selectByOrderId(orderId))
                        .stream()
                        .map(this::toFulfillmentRecordRespDTO)
                        .toList(),
                chatDao.selectOfficialByOrderId(orderId, officialMessageProperties.getSystemSenderId()).stream()
                        .map(this::toOfficialMessageRespDTO)
                        .toList());
    }

    private FulfillmentRecordRespDTO toFulfillmentRecordRespDTO(FulfillmentRecordDO record) {
        return new FulfillmentRecordRespDTO(
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
                record.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(record.getCreatedAt()));
    }

    private OfficialMessageRespDTO toOfficialMessageRespDTO(ChatDO chat) {
        return new OfficialMessageRespDTO(
                chat.getMessageId(),
                chat.getOrderId(),
                chat.getSenderId(),
                chat.getReceiverId(),
                chat.getContent(),
                chat.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(chat.getCreatedAt()));
    }
}
