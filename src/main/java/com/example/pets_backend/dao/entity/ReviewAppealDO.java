package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("review_appeals")
public class ReviewAppealDO extends BaseDO {

    @TableId(value = "appeal_id", type = IdType.ASSIGN_ID)
    private Long appealId;
    private Long reviewId;
    private Long orderId;
    private Long providerId;
    private Long ownerId;
    private String reason;
    private String evidenceUrls;
    private Integer appealStatus;
    private String adminMemo;
    private LocalDateTime appealDeadline;
    private LocalDateTime closedAt;
}
