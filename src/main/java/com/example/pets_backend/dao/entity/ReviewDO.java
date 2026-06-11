package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("reviews")
public class ReviewDO extends BaseDO {

    @TableId(value = "review_id", type = IdType.ASSIGN_ID)
    private Long reviewId;
    private Long orderId;
    private Long reviewerId;
    private Long targetId;
    private Integer score;
    private Integer overallScore;
    private Integer punctualityScore;
    private Integer professionalScore;
    private Integer isLowScore;
    private Integer reviewStatus;
    private String content;
    private Integer reviewType;
}
