package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("order_settlements")
public class OrderSettlementDO extends BaseDO {

    @TableId(value = "settlement_id", type = IdType.ASSIGN_ID)
    private Long settlementId;
    private Long orderId;
    private Long ownerId;
    private Long providerId;
    private BigDecimal grossAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal providerIncome;
    private Integer settlementStatus;
    private LocalDateTime settledAt;
}
