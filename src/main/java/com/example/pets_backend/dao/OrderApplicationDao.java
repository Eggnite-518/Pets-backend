package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.mapper.OrderApplicationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderApplicationDao {

    private final OrderApplicationMapper orderApplicationMapper;

    public void insert(OrderApplicationDO application) {
        orderApplicationMapper.insert(application);
    }

    public boolean existsByOrderIdAndProviderId(Long orderId, Long providerId) {
        return orderApplicationMapper.countByOrderIdAndProviderId(orderId, providerId) > 0;
    }

    public int deleteByOrderIdAndProviderId(Long orderId, Long providerId) {
        return orderApplicationMapper.deleteByOrderIdAndProviderId(orderId, providerId);
    }

    public List<OrderApplicationDO> selectByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderApplicationDO::getOrderId, orderIds);
        return orderApplicationMapper.selectList(wrapper);
    }

    public List<OrderApplicationDO> selectApplyingByProviderId(Long providerId) {
        if (providerId == null) {
            return List.of();
        }
        LambdaQueryWrapper<OrderApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderApplicationDO::getProviderId, providerId)
                .eq(OrderApplicationDO::getApplyStatus, 0)
                .orderByDesc(OrderApplicationDO::getApplyId);
        return orderApplicationMapper.selectList(wrapper);
    }

    public List<OrderApplicationDO> selectApplyingByOrderId(Long orderId) {
        if (orderId == null) {
            return List.of();
        }
        LambdaQueryWrapper<OrderApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderApplicationDO::getOrderId, orderId)
                .eq(OrderApplicationDO::getApplyStatus, 0)
                .orderByAsc(OrderApplicationDO::getApplyId);
        return orderApplicationMapper.selectList(wrapper);
    }

    public OrderApplicationDO selectByOrderIdAndProviderId(Long orderId, Long providerId) {
        LambdaQueryWrapper<OrderApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderApplicationDO::getOrderId, orderId)
                .eq(OrderApplicationDO::getProviderId, providerId);
        return orderApplicationMapper.selectOne(wrapper);
    }

    public int updateApplyStatus(Long orderId, Long providerId, Integer applyStatus) {
        return orderApplicationMapper.updateApplyStatusByOrderIdAndProviderId(orderId, providerId, applyStatus);
    }

    public int updateApplyStatusForOthers(Long orderId, Long excludeProviderId, Integer applyStatus) {
        return orderApplicationMapper.updateApplyStatusForOthers(orderId, excludeProviderId, applyStatus);
    }
}
