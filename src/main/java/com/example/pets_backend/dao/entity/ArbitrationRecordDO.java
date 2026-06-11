package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("arbitration_records")
public class ArbitrationRecordDO extends BaseDO {

    @TableId(value = "arbitration_id", type = IdType.ASSIGN_ID)
    private Long arbitrationId;
    private Long orderId;
    private Long plaintiffId;
    private Long defendantId;
    @TableField("arb_type")
    private Integer arbType;
    private String reason;
    private String evidenceUrls;
    private Integer arbitrationStatus;
    private Integer resultType;
    private String adminMemo;
    private LocalDateTime closedAt;
}
