package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.mapper.OrderMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderDao {

    private final OrderMapper orderMapper;

    public void insert(OrderDO order) {
        orderMapper.insert(order);
    }

    public OrderDO selectById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    public List<OrderDO> selectByOwnerIdAndStatus(Long ownerId, Integer status) {
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getOwnerId, ownerId)
                .eq(OrderDO::getStatus, status)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public IPage<OrderDO> selectOpenOrderPage(int page, int pageSize) {
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getStatus, 1)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public IPage<OrderDO> selectOpenOrderPageByFilters(int page, int pageSize, Integer petType, Integer serviceType) {
        return orderMapper.selectOpenOrderPageByFilters(new Page<>(page, pageSize), petType, serviceType);
    }

    public int updateProviderIdAndStatus(Long orderId, Long providerId, Integer status) {
        return orderMapper.updateProviderIdAndStatus(orderId, providerId, status);
    }

    public int updateStatus(Long orderId, Integer status) {
        return orderMapper.updateStatus(orderId, status);
    }

    public List<OrderDO> selectByStatus(Integer status) {
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getStatus, status)
                .orderByAsc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public List<OrderDO> selectByStatuses(List<Integer> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderDO::getStatus, statuses)
                .orderByAsc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public List<Long> selectInactiveProviders(LocalDateTime cutoff) {
        return orderMapper.selectInactiveProviders(cutoff);
    }

    public List<OrderDO> selectByProviderId(Long providerId) {
        if (providerId == null) {
            return List.of();
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getProviderId, providerId)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public List<OrderDO> selectByProviderIdAndStatuses(Long providerId, List<Integer> statuses) {
        if (providerId == null || statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getProviderId, providerId)
                .in(OrderDO::getStatus, statuses)
                .orderByAsc(OrderDO::getServiceDate)
                .orderByAsc(OrderDO::getServiceStartTime)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public List<OrderDO> selectByIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderDO::getOrderId, orderIds)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public List<OrderDO> selectByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return List.of();
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getOwnerId, ownerId)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public long countByProviderIdAndStatus(Long providerId, Integer status) {
        if (providerId == null || status == null) {
            return 0L;
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getProviderId, providerId)
                .eq(OrderDO::getStatus, status);
        Long count = orderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    public List<OrderDO> selectByProviderIdStatusAndServiceDate(Long providerId, Integer status,
            LocalDate serviceDate) {
        if (providerId == null || status == null || serviceDate == null) {
            return List.of();
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getProviderId, providerId)
                .eq(OrderDO::getStatus, status)
                .eq(OrderDO::getServiceDate, serviceDate)
                .orderByAsc(OrderDO::getServiceStartTime)
                .orderByDesc(OrderDO::getOrderId);
        return orderMapper.selectList(wrapper);
    }

    public long countByProviderIdAndCreatedBetween(Long providerId, LocalDateTime start, LocalDateTime end) {
        if (providerId == null || start == null || end == null) {
            return 0L;
        }
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getProviderId, providerId)
                .between(OrderDO::getCreatedAt, start, end);
        Long count = orderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    public long countOngoingByUserId(Long userId, int statusMin, int statusMax) {
        if (userId == null) return 0L;
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(OrderDO::getOwnerId, userId).or().eq(OrderDO::getProviderId, userId))
                .between(OrderDO::getStatus, statusMin, statusMax);
        Long count = orderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }
}
