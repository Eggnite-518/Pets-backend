package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("pet_owners")
public class PetOwnerDO extends BaseDO {

    @TableId(value = "owner_id", type = IdType.INPUT)
    private Long ownerId;

    private String emergencyContact;
}

