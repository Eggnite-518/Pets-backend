package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("user_addresses")
public class UserAddressDO extends BaseDO {

    @TableId(value = "address_id", type = IdType.ASSIGN_ID)
    private Long addressId;
    private Long userId;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String addressTag;
    private Integer isDefault;
    private Double latitude;
    private Double longitude;
    private String sopRequirementTagsJson;
    private String sopHardFilterTags;
    private String sopRemark;
    private java.time.LocalDateTime sopUpdatedAt;
}
