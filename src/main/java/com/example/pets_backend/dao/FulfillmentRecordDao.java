package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.mapper.FulfillmentRecordMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FulfillmentRecordDao {

    private final FulfillmentRecordMapper fulfillmentRecordMapper;

    public void insert(FulfillmentRecordDO record) {
        fulfillmentRecordMapper.insert(record);
    }

    public void updateById(FulfillmentRecordDO record) {
        fulfillmentRecordMapper.updateById(record);
    }

    public List<FulfillmentRecordDO> selectByOrderId(Long orderId) {
        LambdaQueryWrapper<FulfillmentRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FulfillmentRecordDO::getOrderId, orderId)
                .orderByAsc(FulfillmentRecordDO::getCreatedAt);
        return fulfillmentRecordMapper.selectList(wrapper);
    }

    public void deleteFailedByOrderIdAndNodeType(Long orderId, Integer nodeType) {
        LambdaQueryWrapper<FulfillmentRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FulfillmentRecordDO::getOrderId, orderId)
                .eq(FulfillmentRecordDO::getNodeType, nodeType)
                .eq(FulfillmentRecordDO::getProcessingStatus, "FAILED");
        fulfillmentRecordMapper.delete(wrapper);
    }

    public boolean existsByOrderIdAndNodeType(Long orderId, Integer nodeType) {
        LambdaQueryWrapper<FulfillmentRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FulfillmentRecordDO::getOrderId, orderId)
                .eq(FulfillmentRecordDO::getNodeType, nodeType)
                .last("LIMIT 1");
        return fulfillmentRecordMapper.selectOne(wrapper) != null;
    }

    public void deleteDemoSeedByOrderIdAndNodeType(Long orderId, Integer nodeType) {
        LambdaQueryWrapper<FulfillmentRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FulfillmentRecordDO::getOrderId, orderId)
                .eq(FulfillmentRecordDO::getNodeType, nodeType)
                .and(w -> w.like(FulfillmentRecordDO::getObjectKey, "/demo/")
                        .or()
                        .likeRight(FulfillmentRecordDO::getObjectKey, "seed/")
                        .or()
                        .like(FulfillmentRecordDO::getImageUrl, "/seed/default/"));
        fulfillmentRecordMapper.delete(wrapper);
    }

    public FulfillmentRecordDO selectLatestByOrderId(Long orderId) {
        LambdaQueryWrapper<FulfillmentRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FulfillmentRecordDO::getOrderId, orderId)
                .orderByDesc(FulfillmentRecordDO::getCreatedAt)
                .last("LIMIT 1");
        return fulfillmentRecordMapper.selectOne(wrapper);
    }
}
