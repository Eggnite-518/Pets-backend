package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("order_address_snapshots")
public class OrderAddressSnapshotDO extends BaseDO {

    @TableId(value = "snapshot_id", type = IdType.ASSIGN_ID)
    private Long snapshotId;
    private Long sourceAddressId;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String addressTag;
    private Double latitude;
    private Double longitude;
}
