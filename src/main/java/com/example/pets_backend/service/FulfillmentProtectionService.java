package com.example.pets_backend.service;

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
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.official.OfficialMessageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillmentProtectionService {

    private static final int ORDER_STATUS_ON_THE_WAY = OrderStatusEnum.PENDING_FULFILLMENT.getCode();
    private static final int ORDER_STATUS_IN_SERVICE = OrderStatusEnum.IN_FULFILLMENT.getCode();
    private static final int ORDER_STATUS_BLOCKED_WAIT_OWNER = OrderStatusEnum.BLOCKED_WAIT_OWNER.getCode();
    private static final int ORDER_STATUS_EXCEPTION_ENDED = OrderStatusEnum.EXCEPTION_ENDED.getCode();
    private static final int ORDER_STATUS_EMERGENCY_PLATFORM = OrderStatusEnum.EMERGENCY_PLATFORM_INTERVENTION.getCode();

    private static final int NODE_TYPE_ARRIVED = 1;
    private static final int NODE_TYPE_AUTO_RISK_ALERT = 90;
    private static final int NODE_TYPE_OWNER_CONFIRMED = 92;
    private static final int NODE_TYPE_NO_FAULT_RETREAT = 93;
    private static final int NODE_TYPE_EMERGENCY_EXCEPTION = 94;

    private final OrderDao orderDao;
    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final ExceptionReportDao exceptionReportDao;
    private final FulfillmentProtectionProperties protectionProperties;
    private final OfficialMessageService officialMessageService;
    private final OrderSettlementService orderSettlementService;

    @Transactional
    public void scanAndProtectOrders() {
        scanPossibleLateOrders();
        scanInactiveOrders();
    }

    @Transactional
    public void selfReportException(Long orderId, SelfReportExceptionReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.exceptionType() == null || reqDTO.description() == null
                || reqDTO.description().isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        EmergencyExceptionTypeEnum exceptionType = EmergencyExceptionTypeEnum.fromCode(reqDTO.exceptionType());
        OrderDO order = requireProviderOrder(orderId);
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_IN_SERVICE)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        Long providerId = currentUserId();
        orderDao.updateStatus(order.getOrderId(), ORDER_STATUS_EMERGENCY_PLATFORM);
        persistExceptionReport(order, providerId, exceptionType, reqDTO.description());
        String detail = buildEmergencyReportDetail(exceptionType, reqDTO.description());
        appendSystemRecord(order.getOrderId(), NODE_TYPE_EMERGENCY_EXCEPTION, detail);
        notifyOwnerByOfficialMessage(order, "紧急求助-" + exceptionType.getDesc(), detail,
                OrderStatusEnum.EMERGENCY_PLATFORM_INTERVENTION.getDesc());
        log.warn("Order moved to emergency platform intervention, orderId={}, exceptionType={}",
                order.getOrderId(), exceptionType.getDesc());
    }

    @Transactional
    public void ownerConfirmResolved(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_BLOCKED_WAIT_OWNER)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        orderDao.updateStatus(orderId, ORDER_STATUS_IN_SERVICE);
        appendSystemRecord(orderId, NODE_TYPE_OWNER_CONFIRMED, "宠主已确认恢复服务");
        notifyProviderByOfficialMessage(order, "异常已解决",
                "宠主已确认当前问题已处理完成，订单恢复为服务中",
                OrderStatusEnum.IN_FULFILLMENT.getDesc());
    }

    @Transactional
    public void noFaultRetreat(Long orderId) {
        OrderDO order = requireProviderOrder(orderId);
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_BLOCKED_WAIT_OWNER)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        LocalDateTime updatedAt = order.getUpdatedAt();
        if (updatedAt == null
                || updatedAt.isAfter(LocalDateTime.now().minusMinutes(protectionProperties.getOwnerResponseTimeoutMinutes()))) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        orderSettlementService.settleExceptionEnded(orderId);
        orderDao.updateStatus(orderId, ORDER_STATUS_EXCEPTION_ENDED);
        appendSystemRecord(orderId, NODE_TYPE_NO_FAULT_RETREAT, "履约官无责撤退");
        notifyOwnerByOfficialMessage(order, "异常结束",
                "宠主超时未处理，服务者已发起无责撤退。系统已按空跑补偿规则完成结算",
                OrderStatusEnum.EXCEPTION_ENDED.getDesc());
    }

    private void scanPossibleLateOrders() {
        LocalDateTime arrivalDeadline = LocalDateTime.now().minusMinutes(protectionProperties.getArrivalTimeoutMinutes());
        List<OrderDO> pendingOrders = orderDao.selectByStatus(ORDER_STATUS_ON_THE_WAY);
        for (OrderDO order : pendingOrders) {
            if (order.getCreatedAt() == null || order.getCreatedAt().isAfter(arrivalDeadline)) {
                continue;
            }
            boolean arrived = fulfillmentRecordDao.existsByOrderIdAndNodeType(order.getOrderId(), NODE_TYPE_ARRIVED);
            if (!arrived) {
                moveToBlockedAndAlert(order, "可能迟到", "系统预警: 超时未到达");
            }
        }
    }

    private void scanInactiveOrders() {
        LocalDateTime inactivityDeadline = LocalDateTime.now().minusMinutes(protectionProperties.getInactivityTimeoutMinutes());
        List<OrderDO> inServiceOrders = orderDao.selectByStatus(ORDER_STATUS_IN_SERVICE);
        for (OrderDO order : inServiceOrders) {
            LocalDateTime lastActiveAt = resolveLastActiveAt(order.getOrderId(), order.getUpdatedAt());
            if (lastActiveAt != null && lastActiveAt.isBefore(inactivityDeadline)) {
                moveToBlockedAndAlert(order, "风控关注", "系统预警: 中途长时间无动作");
            }
        }
    }

    private void moveToBlockedAndAlert(OrderDO order, String smsReason, String detail) {
        if (Objects.equals(order.getStatus(), ORDER_STATUS_BLOCKED_WAIT_OWNER)) {
            return;
        }
        orderDao.updateStatus(order.getOrderId(), ORDER_STATUS_BLOCKED_WAIT_OWNER);
        appendSystemRecord(order.getOrderId(), NODE_TYPE_AUTO_RISK_ALERT, detail);
        notifyOwnerByOfficialMessage(order, smsReason, detail, OrderStatusEnum.BLOCKED_WAIT_OWNER.getDesc());
        log.warn("Order moved to blocked status, orderId={}, detail={}", order.getOrderId(), detail);
    }

    private LocalDateTime resolveLastActiveAt(Long orderId, LocalDateTime fallback) {
        FulfillmentRecordDO latestRecord = fulfillmentRecordDao.selectLatestByOrderId(orderId);
        if (latestRecord != null && latestRecord.getCreatedAt() != null) {
            return latestRecord.getCreatedAt();
        }
        return fallback;
    }

    private void notifyOwnerByOfficialMessage(OrderDO order, String title, String detail, String statusDesc) {
        officialMessageService.sendSystemOfficialMessage(order.getOrderId(), order.getOwnerId(),
                buildOwnerNotificationContent(order, title, detail, statusDesc));
    }

    private void notifyProviderByOfficialMessage(OrderDO order, String title, String detail, String statusDesc) {
        if (order.getProviderId() == null) {
            return;
        }
        officialMessageService.sendSystemOfficialMessage(order.getOrderId(), order.getProviderId(),
                buildProviderNotificationContent(order, title, detail, statusDesc));
    }

    private void persistExceptionReport(OrderDO order, Long reporterId, EmergencyExceptionTypeEnum exceptionType,
            String description) {
        ExceptionReportDO report = new ExceptionReportDO();
        report.setOrderId(order.getOrderId());
        report.setReporterId(reporterId);
        report.setExceptionType(exceptionType.getCode());
        report.setDescription(description.trim());
        report.setReportStatus(ExceptionReportStatusEnum.PENDING.getCode());
        exceptionReportDao.insert(report);
    }

    private String buildEmergencyReportDetail(EmergencyExceptionTypeEnum exceptionType, String description) {
        return "紧急求助 " + exceptionType.getDesc() + "，desc=" + description;
    }

    private String buildOwnerNotificationContent(OrderDO order, String title, String detail, String statusDesc) {
        return "【系统通知】订单" + order.getOrderId() + title + "，" + detail + "。当前状态已变更为" + statusDesc + "，请尽快处理。";
    }

    private String buildProviderNotificationContent(OrderDO order, String title, String detail, String statusDesc) {
        return "【系统通知】订单" + order.getOrderId() + title + "，" + detail + "。当前状态已变更为" + statusDesc + "，请按最新指引继续处理。";
    }

    private void appendSystemRecord(Long orderId, Integer nodeType, String detail) {
        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setOrderId(orderId);
        record.setNodeType(nodeType);
        record.setMediaType("SYSTEM_EVENT");
        record.setProcessingStatus("SUCCESS");
        record.setProcessingError(detail);
        fulfillmentRecordDao.insert(record);
    }

    private OrderDO requireProviderOrder(Long orderId) {
        Long providerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!providerId.equals(order.getProviderId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        return order;
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}


