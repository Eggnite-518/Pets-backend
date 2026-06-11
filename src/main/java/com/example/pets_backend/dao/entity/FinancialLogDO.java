package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("financial_logs")
public class FinancialLogDO extends BaseDO {

    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    private Long logId;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Integer tradeType;
    private Long relationId;
}
