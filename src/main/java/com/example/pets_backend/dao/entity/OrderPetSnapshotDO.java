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
@TableName("order_pets_snapshot")
public class OrderPetSnapshotDO extends BaseDO {

    @TableId(value = "snapshot_id", type = IdType.ASSIGN_ID)
    private Long snapshotId;
    private Long orderId;
    private Long archivePetId;
    private String snapPetName;
    private Integer snapPetType;
    private String snapReq;
    @TableField("snap_profile_tags_json")
    private String snapProfileTagsJson;
}
