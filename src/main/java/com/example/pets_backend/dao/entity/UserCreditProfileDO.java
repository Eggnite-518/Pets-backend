package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("user_credit_profiles")
public class UserCreditProfileDO extends BaseDO {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Double scoreAvg;
    private Integer ratingCount;
}

