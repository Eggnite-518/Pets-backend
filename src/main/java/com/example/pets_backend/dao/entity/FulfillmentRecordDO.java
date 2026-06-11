package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("fulfillment_records")
public class FulfillmentRecordDO extends BaseDO {

    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    private Long recordId;
    private Long orderId;
    private Integer nodeType;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String mediaType;
    private String objectKey;
    private Long fileSize;
    private String contentType;
    private Integer frameRate;
    private String processingStatus;
    private String processingErrorCode;
    private String processingError;
    private String originalObjectKey;
    private String originalContentType;
    private Long originalFileSize;
    private String watermarkText;
}
