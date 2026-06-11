package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.mapper.OrderPetSnapshotMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderPetSnapshotDao {

    private final OrderPetSnapshotMapper orderPetSnapshotMapper;

    public void insert(OrderPetSnapshotDO snapshot) {
        orderPetSnapshotMapper.insert(snapshot);
    }

    public List<OrderPetSnapshotDO> selectByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderPetSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderPetSnapshotDO::getOrderId, orderIds);
        return orderPetSnapshotMapper.selectList(wrapper);
    }
}
