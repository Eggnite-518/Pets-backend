package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.mapper.SitterProfileMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SitterProfileDao {

    private final SitterProfileMapper mapper;

    public SitterProfileDO selectById(Long providerId) {
        return mapper.selectById(providerId);
    }

    public List<SitterProfileDO> selectByIds(List<Long> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectBatchIds(providerIds);
    }

    public void insert(SitterProfileDO profile) {
        mapper.insert(profile);
    }

    public void updateById(SitterProfileDO profile) {
        mapper.updateById(profile);
    }

    public List<SitterProfileDO> selectVerifiedActiveProviders() {
        LambdaQueryWrapper<SitterProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SitterProfileDO::getVerifyStatus, 2)
                .and(w -> w.eq(SitterProfileDO::getIsBanned, 0).or().isNull(SitterProfileDO::getIsBanned));
        return mapper.selectList(wrapper);
    }
}
