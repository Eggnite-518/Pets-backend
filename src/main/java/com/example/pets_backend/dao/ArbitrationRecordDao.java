package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.ArbitrationRecordDO;
import com.example.pets_backend.dao.mapper.ArbitrationRecordMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArbitrationRecordDao {

    private final ArbitrationRecordMapper arbitrationRecordMapper;

    public void insert(ArbitrationRecordDO record) {
        arbitrationRecordMapper.insert(record);
    }

    public List<ArbitrationRecordDO> selectByOrderId(Long orderId) {
        LambdaQueryWrapper<ArbitrationRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArbitrationRecordDO::getOrderId, orderId)
                .orderByDesc(ArbitrationRecordDO::getCreatedAt);
        return arbitrationRecordMapper.selectList(wrapper);
    }

    public boolean existsActiveByOrderIdAndPlaintiffId(Long orderId, Long plaintiffId) {
        LambdaQueryWrapper<ArbitrationRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArbitrationRecordDO::getOrderId, orderId)
                .eq(ArbitrationRecordDO::getPlaintiffId, plaintiffId)
                .in(ArbitrationRecordDO::getArbitrationStatus, List.of(0, 1, 2))
                .last("LIMIT 1");
        return arbitrationRecordMapper.selectOne(wrapper) != null;
    }

    public boolean existsActiveByParticipant(Long participantId) {
        if (participantId == null) {
            return false;
        }
        LambdaQueryWrapper<ArbitrationRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(condition -> condition.eq(ArbitrationRecordDO::getPlaintiffId, participantId)
                .or()
                .eq(ArbitrationRecordDO::getDefendantId, participantId))
                .in(ArbitrationRecordDO::getArbitrationStatus, List.of(0, 1, 2))
                .last("LIMIT 1");
        return arbitrationRecordMapper.selectOne(wrapper) != null;
    }

    public boolean existsUnclosedClaimByParticipant(Long participantId) {
        if (participantId == null) {
            return false;
        }
        LambdaQueryWrapper<ArbitrationRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(condition -> condition.eq(ArbitrationRecordDO::getPlaintiffId, participantId)
                .or()
                .eq(ArbitrationRecordDO::getDefendantId, participantId))
                .in(ArbitrationRecordDO::getArbType, List.of(2, 3, 5))
                .in(ArbitrationRecordDO::getArbitrationStatus, List.of(0, 1, 2))
                .last("LIMIT 1");
        return arbitrationRecordMapper.selectOne(wrapper) != null;
    }
}
