package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.ExceptionReportDO;
import com.example.pets_backend.dao.mapper.ExceptionReportMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExceptionReportDao {

    private final ExceptionReportMapper exceptionReportMapper;

    public void insert(ExceptionReportDO report) {
        exceptionReportMapper.insert(report);
    }

    public boolean existsPendingByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<ExceptionReportDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ExceptionReportDO::getOrderId, orderIds)
                .in(ExceptionReportDO::getReportStatus, List.of(0, 1))
                .last("LIMIT 1");
        return exceptionReportMapper.selectOne(wrapper) != null;
    }
}
