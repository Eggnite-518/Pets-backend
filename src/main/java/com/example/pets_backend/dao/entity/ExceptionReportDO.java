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
@TableName("exception_reports")
public class ExceptionReportDO extends BaseDO {

    @TableId(value = "report_id", type = IdType.ASSIGN_ID)
    private Long reportId;
    private Long orderId;
    private Long reporterId;
    private Integer exceptionType;
    private String description;
    private String proofImages;
    private Integer reportStatus;
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;
}
