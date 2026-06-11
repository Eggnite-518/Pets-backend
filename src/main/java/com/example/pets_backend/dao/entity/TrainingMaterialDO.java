package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("training_materials")
public class TrainingMaterialDO extends BaseDO {

    @TableId(value = "material_id", type = IdType.ASSIGN_ID)
    private Long materialId;
    private String title;
    private String content;
    private Integer sortOrder;
    private String materialType;
    private String moduleCode;
    private Integer minDurationSeconds;
    private String mediaUrl;
    private Integer isRequired;
}
