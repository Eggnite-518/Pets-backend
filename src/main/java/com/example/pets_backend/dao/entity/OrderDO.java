package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("orders")
public class OrderDO extends BaseDO {

    @TableId(value = "order_id", type = IdType.ASSIGN_ID)
    private Long orderId;
    private Long ownerId;
    private Long providerId;
    private Long addressSnapshotId;
    @TableField("order_status")
    private Integer status;
    private BigDecimal totalAmount;
    private LocalDate serviceDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private Integer serviceType;
    @TableField("hard_filter_tags")
    private String hardFilterTags;
    @TableField("requirement_tags_json")
    private String requirementTagsJson;
}
