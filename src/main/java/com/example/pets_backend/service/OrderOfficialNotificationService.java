package com.example.pets_backend.service;

import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.service.official.OfficialMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOfficialNotificationService {

    private final OfficialMessageService officialMessageService;
    private final UserDao userDao;

    public void notifyOwnerOnProviderApplication(OrderDO order, Long providerId) {
        if (order == null || order.getOrderId() == null || order.getOwnerId() == null || providerId == null) {
            return;
        }
        String providerName = resolveNickname(providerId);
        String content = "【报名通知】宠托师" + providerName + "已报名您的订单 #" + order.getOrderId()
                + "，请前往订单详情查看候选人并录用。";
        sendSafely(order.getOrderId(), order.getOwnerId(), content);
    }

    public void notifyCaretakerOnSelected(OrderDO order, Long providerId) {
        if (order == null || order.getOrderId() == null || providerId == null) {
            return;
        }
        String serviceDate = order.getServiceDate() == null ? "待定" : order.getServiceDate().toString();
        String content = "【录用通知】恭喜！您已被宠主录用为订单 #" + order.getOrderId()
                + " 的服务宠托师，服务日期 " + serviceDate + "。请等待宠主完成付款后按约定时间履约。";
        sendSafely(order.getOrderId(), providerId, content);
    }

    private void sendSafely(Long orderId, Long receiverId, String content) {
        try {
            officialMessageService.sendSystemOfficialMessage(orderId, receiverId, content);
        } catch (RuntimeException ex) {
            log.warn("Failed to send order official message, orderId={}, receiverId={}", orderId, receiverId, ex);
        }
    }

    private String resolveNickname(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null || user.getNickname() == null || user.getNickname().isBlank()) {
            return "一位宠托师";
        }
        return user.getNickname().trim();
    }
}
