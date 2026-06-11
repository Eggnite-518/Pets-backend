package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.PlatformFinancialLogDO;
import com.example.pets_backend.dao.mapper.PlatformFinancialLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlatformFinancialLogDao {

    private final PlatformFinancialLogMapper platformFinancialLogMapper;

    public void insert(PlatformFinancialLogDO platformFinancialLog) {
        platformFinancialLogMapper.insert(platformFinancialLog);
    }

    public PlatformFinancialLogDO selectLatest() {
        LambdaQueryWrapper<PlatformFinancialLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PlatformFinancialLogDO::getCreatedAt)
                .orderByDesc(PlatformFinancialLogDO::getLogId)
                .last("LIMIT 1");
        return platformFinancialLogMapper.selectOne(wrapper);
    }
}
