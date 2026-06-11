package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.OrderSettlementDO;
import com.example.pets_backend.dao.mapper.OrderSettlementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderSettlementDao {

    private final OrderSettlementMapper orderSettlementMapper;

    public void insert(OrderSettlementDO settlement) {
        orderSettlementMapper.insert(settlement);
    }

    public void updateById(OrderSettlementDO settlement) {
        orderSettlementMapper.updateById(settlement);
    }

    public OrderSettlementDO selectByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        LambdaQueryWrapper<OrderSettlementDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderSettlementDO::getOrderId, orderId)
                .last("LIMIT 1");
        return orderSettlementMapper.selectOne(wrapper);
    }
}
