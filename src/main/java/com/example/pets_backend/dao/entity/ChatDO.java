package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("chats")
public class ChatDO extends BaseDO {

    @TableId(value = "message_id", type = IdType.ASSIGN_ID)
    private Long messageId;
    private Long orderId;
    private Long senderId;
    private Long receiverId;
    private String content;
    @TableField("is_read")
    private Boolean isRead;
    @TableField("sent_at")
    private LocalDateTime sentAt;
}
