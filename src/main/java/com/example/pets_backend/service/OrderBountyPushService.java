package com.example.pets_backend.service;

import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.service.official.OfficialMessageService;
import com.example.pets_backend.service.support.OrderHardFilterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBountyPushService {

    private final OrderHardFilterService orderHardFilterService;
    private final OrderAddressSnapshotDao orderAddressSnapshotDao;
    private final OfficialMessageService officialMessageService;

    public void notifyEligibleProviders(OrderDO order) {
        if (order == null || order.getOrderId() == null) {
            return;
        }
        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        List<Long> providerIds = orderHardFilterService.findEligibleProviderIds(order, addressSnapshot);
        if (providerIds.isEmpty()) {
            log.info("No eligible providers for bounty push, orderId={}", order.getOrderId());
            return;
        }
        String content = buildPushContent(order);
        for (Long providerId : providerIds) {
            try {
                officialMessageService.sendSystemOfficialMessage(order.getOrderId(), providerId, content);
            } catch (RuntimeException ex) {
                log.warn("Failed to push bounty order, orderId={}, providerId={}", order.getOrderId(), providerId, ex);
            }
        }
        log.info("Bounty push completed, orderId={}, providerCount={}", order.getOrderId(), providerIds.size());
    }

    private String buildPushContent(OrderDO order) {
        String serviceDate = order.getServiceDate() == null ? "待定" : order.getServiceDate().toString();
        return "【新悬赏单】附近有新订单 #" + order.getOrderId()
                + " 符合您的服务条件，服务日期 " + serviceDate + "，请前往抢单大厅查看。";
    }
}
