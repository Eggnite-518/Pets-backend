package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("pet_archives")
public class PetArchiveDO extends BaseDO {

    @TableId(value = "pet_id", type = IdType.ASSIGN_ID)
    private Long petId;
    private Long ownerId;
    private String petName;
    private Integer petType;
    private String defaultReq;
    private String image;
    @TableField("profile_tags_json")
    private String profileTagsJson;
}

