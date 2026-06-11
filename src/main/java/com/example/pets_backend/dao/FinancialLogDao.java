package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pets_backend.dao.entity.FinancialLogDO;
import com.example.pets_backend.dao.mapper.FinancialLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FinancialLogDao {

    private final FinancialLogMapper financialLogMapper;

    public void insert(FinancialLogDO financialLog) {
        financialLogMapper.insert(financialLog);
    }

    public IPage<FinancialLogDO> selectByUserId(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<FinancialLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancialLogDO::getUserId, userId)
                .orderByDesc(FinancialLogDO::getCreatedAt)
                .orderByDesc(FinancialLogDO::getLogId);
        return financialLogMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public FinancialLogDO selectLatestByUserIdAndTradeType(Long userId, Integer tradeType) {
        if (userId == null || tradeType == null) {
            return null;
        }
        LambdaQueryWrapper<FinancialLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancialLogDO::getUserId, userId)
                .eq(FinancialLogDO::getTradeType, tradeType)
                .orderByDesc(FinancialLogDO::getCreatedAt)
                .orderByDesc(FinancialLogDO::getLogId)
                .last("LIMIT 1");
        return financialLogMapper.selectOne(wrapper);
    }
}
