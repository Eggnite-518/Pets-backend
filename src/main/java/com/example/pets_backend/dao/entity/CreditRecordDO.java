package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("credit_records")
public class CreditRecordDO extends BaseDO {

    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    private Long recordId;
    private Long providerId;
    private Integer changeScore;
    private Integer scoreAfter;
    private Integer reasonType;
    private Long relationId;
}

