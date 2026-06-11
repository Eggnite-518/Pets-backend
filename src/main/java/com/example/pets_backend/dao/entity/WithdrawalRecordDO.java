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
@TableName("withdrawal_records")
public class WithdrawalRecordDO extends BaseDO {

    @TableId(value = "withdraw_id", type = IdType.ASSIGN_ID)
    private Long withdrawId;
    private Long userId;
    private BigDecimal amount;
    private Integer withdrawalStatus;
    private Integer accountType;
    private String accountInfo;
}
