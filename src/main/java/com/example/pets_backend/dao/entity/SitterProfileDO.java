package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sitter_profiles")
public class SitterProfileDO extends BaseDO {

    @TableId("provider_id")
    private Long providerId;
    private Integer gender;
    @TableField("verify_status")
    private Integer verifyStatus;
    @TableField("deposit_amount")
    private BigDecimal depositAmount;
    @TableField("credit_score")
    private Integer creditScore;
    @TableField("is_banned")
    private Integer isBanned;
    @TableField("service_radius_km")
    private Integer serviceRadiusKm;
    @TableField("resident_address")
    private String residentAddress;
    @TableField("resident_latitude")
    private BigDecimal residentLatitude;
    @TableField("resident_longitude")
    private BigDecimal residentLongitude;
    @TableField("cert_labels_json")
    private String certLabelsJson;
}
