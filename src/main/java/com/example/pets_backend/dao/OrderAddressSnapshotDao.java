package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.mapper.OrderAddressSnapshotMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderAddressSnapshotDao {

    private final OrderAddressSnapshotMapper orderAddressSnapshotMapper;

    public OrderAddressSnapshotDO selectById(Long snapshotId) {
        return orderAddressSnapshotMapper.selectById(snapshotId);
    }

    public List<OrderAddressSnapshotDO> selectByIds(List<Long> snapshotIds) {
        if (snapshotIds == null || snapshotIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<OrderAddressSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderAddressSnapshotDO::getSnapshotId, snapshotIds);
        return orderAddressSnapshotMapper.selectList(wrapper);
    }

    public void insert(OrderAddressSnapshotDO addressSnapshot) {
        orderAddressSnapshotMapper.insert(addressSnapshot);
    }
}
