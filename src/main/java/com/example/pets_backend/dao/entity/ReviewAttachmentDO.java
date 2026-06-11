package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("review_attachments")
public class ReviewAttachmentDO extends BaseDO {

    @TableId(value = "attachment_id", type = IdType.ASSIGN_ID)
    private Long attachmentId;
    private Long reviewId;
    private String url;
    private String objectKey;
    private String mediaType;
    private String contentType;
    private Long fileSize;
    private Integer sortOrder;
}
