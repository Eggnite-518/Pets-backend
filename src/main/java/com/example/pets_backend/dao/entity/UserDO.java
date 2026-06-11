package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("users")
public class UserDO extends BaseDO {

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;
    private String phone;
    private String nickname;
    private String passwordHash;
    private String avatarUrl;
    private Integer roleType;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private String realName;
    private String idCardNo;
}
