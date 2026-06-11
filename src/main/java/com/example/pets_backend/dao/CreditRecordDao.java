package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.CreditRecordDO;
import com.example.pets_backend.dao.mapper.CreditRecordMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CreditRecordDao {

    private final CreditRecordMapper mapper;

    public void insert(CreditRecordDO record) {
        mapper.insert(record);
    }

    public List<CreditRecordDO> selectByProviderAndRelation(Long providerId, Long relationId) {
        if (providerId == null || relationId == null) {
            return List.of();
        }
        LambdaQueryWrapper<CreditRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditRecordDO::getProviderId, providerId)
                .eq(CreditRecordDO::getRelationId, relationId)
                .orderByAsc(CreditRecordDO::getCreatedAt)
                .orderByAsc(CreditRecordDO::getRecordId);
        return mapper.selectList(wrapper);
    }
}
