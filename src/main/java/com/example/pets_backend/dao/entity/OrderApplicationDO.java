package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("order_applications")
public class OrderApplicationDO extends BaseDO {

    @TableId(value = "apply_id", type = IdType.ASSIGN_ID)
    private Long applyId;
    private Long orderId;
    private Long providerId;
    private Integer applyStatus;
}
