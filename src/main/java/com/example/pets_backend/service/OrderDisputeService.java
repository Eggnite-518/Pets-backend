package com.example.pets_backend.service;

import com.example.pets_backend.dao.ArbitrationRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.ArbitrationRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.req.SubmitDisputeReqDTO;
import com.example.pets_backend.dto.resp.DisputeRespDTO;
import com.example.pets_backend.dto.resp.EvidenceChainRespDTO;
import com.example.pets_backend.dto.resp.SubmitDisputeRespDTO;
import com.example.pets_backend.enums.OrderStatusEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderDisputeService {

    private static final int STATUS_PENDING = 0;
    private static final int MIN_DISPUTE_ORDER_STATUS = OrderStatusEnum.PENDING_FULFILLMENT.getCode();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderDao orderDao;
    private final ArbitrationRecordDao arbitrationRecordDao;
    private final EvidenceChainService evidenceChainService;

    @Transactional
    public SubmitDisputeRespDTO submitDispute(Long orderId, SubmitDisputeReqDTO reqDTO) {
        Long currentUserId = UserContext.getUserId();
        if (orderId == null || reqDTO == null || currentUserId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        validateRequest(reqDTO);
        OrderDO order = requireOrder(orderId);
        ensureDisputeEligible(order);
        Long defendantId = resolveDefendantId(order, currentUserId);
        if (arbitrationRecordDao.existsActiveByOrderIdAndPlaintiffId(orderId, currentUserId)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        ArbitrationRecordDO record = new ArbitrationRecordDO();
        LocalDateTime now = LocalDateTime.now();
        record.setOrderId(orderId);
        record.setPlaintiffId(currentUserId);
        record.setDefendantId(defendantId);
        record.setArbType(reqDTO.disputeType());
        record.setReason(reqDTO.reason().trim());
        record.setEvidenceUrls(joinEvidenceUrls(reqDTO.evidenceUrls()));
        record.setArbitrationStatus(STATUS_PENDING);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setDeleted(0);
        arbitrationRecordDao.insert(record);
        return new SubmitDisputeRespDTO(record.getArbitrationId(), record.getArbitrationStatus());
    }

    public List<DisputeRespDTO> listDisputes(Long orderId) {
        Long currentUserId = UserContext.getUserId();
        if (orderId == null || currentUserId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        OrderDO order = requireOrder(orderId);
        ensureParticipant(order, currentUserId);
        ensureDisputeEligible(order);
        return arbitrationRecordDao.selectByOrderId(orderId).stream()
                .filter(record -> currentUserId.equals(record.getPlaintiffId()))
                .map(this::toRespDTO)
                .toList();
    }

    public EvidenceChainRespDTO getEvidenceChain(Long orderId) {
        Long currentUserId = UserContext.getUserId();
        if (orderId == null || currentUserId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        OrderDO order = requireOrder(orderId);
        ensureParticipant(order, currentUserId);
        ensureDisputeEligible(order);
        return evidenceChainService.getEvidenceChain(orderId);
    }

    private void validateRequest(SubmitDisputeReqDTO reqDTO) {
        if (reqDTO.disputeType() == null || reqDTO.disputeType() < 1 || reqDTO.disputeType() > 5
                || reqDTO.reason() == null || reqDTO.reason().isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    private Long resolveDefendantId(OrderDO order, Long currentUserId) {
        if (currentUserId.equals(order.getOwnerId())) {
            if (order.getProviderId() == null) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            return order.getProviderId();
        }
        if (currentUserId.equals(order.getProviderId())) {
            return order.getOwnerId();
        }
        throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
    }

    private void ensureParticipant(OrderDO order, Long currentUserId) {
        if (!currentUserId.equals(order.getOwnerId()) && !currentUserId.equals(order.getProviderId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
    }

    private void ensureDisputeEligible(OrderDO order) {
        if (order.getProviderId() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (order.getStatus() == null || order.getStatus() < MIN_DISPUTE_ORDER_STATUS) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private String joinEvidenceUrls(List<String> evidenceUrls) {
        if (evidenceUrls == null || evidenceUrls.isEmpty()) {
            return null;
        }
        return String.join(",", evidenceUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .toList());
    }

    private DisputeRespDTO toRespDTO(ArbitrationRecordDO record) {
        return new DisputeRespDTO(
                record.getArbitrationId(),
                record.getOrderId(),
                record.getPlaintiffId(),
                record.getDefendantId(),
                record.getArbType(),
                disputeTypeDesc(record.getArbType()),
                record.getReason(),
                splitEvidenceUrls(record.getEvidenceUrls()),
                record.getArbitrationStatus(),
                disputeStatusDesc(record.getArbitrationStatus(), record.getClosedAt()),
                record.getResultType(),
                resultTypeDesc(record.getResultType()),
                record.getAdminMemo(),
                record.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(record.getCreatedAt()),
                record.getClosedAt() == null ? null : DATE_TIME_FORMATTER.format(record.getClosedAt()));
    }

    private String disputeTypeDesc(Integer disputeType) {
        return switch (safeInt(disputeType)) {
            case 1 -> "履约打卡异常";
            case 2 -> "服务质量争议";
            case 3 -> "宠物/财物受损";
            case 4 -> "费用与赔付争议";
            case 5 -> "其他严重违规";
            default -> null;
        };
    }

    private String disputeStatusDesc(Integer disputeStatus, LocalDateTime closedAt) {
        if (closedAt != null) {
            return "已结案";
        }
        return switch (safeInt(disputeStatus)) {
            case 0 -> "待受理";
            case 1 -> "取证中";
            case 2 -> "待判定";
            default -> "处理中";
        };
    }

    private String resultTypeDesc(Integer resultType) {
        return switch (safeInt(resultType)) {
            case 1 -> "支持申诉";
            case 2 -> "驳回申诉";
            case 3 -> "部分支持";
            case 4 -> "协商处理";
            default -> null;
        };
    }

    private List<String> splitEvidenceUrls(String evidenceUrls) {
        if (evidenceUrls == null || evidenceUrls.isBlank()) {
            return List.of();
        }
        return Arrays.stream(evidenceUrls.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
