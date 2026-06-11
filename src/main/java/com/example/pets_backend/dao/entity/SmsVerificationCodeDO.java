package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sms_verification_codes")
public class SmsVerificationCodeDO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String phone;
    private String code;
    private Integer used;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
