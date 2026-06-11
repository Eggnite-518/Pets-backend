package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("review_deduction_reasons")
public class ReviewDeductionReasonDO extends BaseDO {

    @TableId(value = "reason_id", type = IdType.ASSIGN_ID)
    private Long reasonId;
    private Long reviewId;
    private Integer reasonType;
    private String reasonText;
}
