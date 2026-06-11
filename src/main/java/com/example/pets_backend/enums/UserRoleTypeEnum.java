package com.example.pets_backend.enums;

import java.util.Arrays;

public enum UserRoleTypeEnum {

    PET_OWNER(1, "仅宠主"),
    SITTER(2, "仅喂养员"),
    BOTH(3, "双重身份");

    private final Integer code;
    private final String desc;

    UserRoleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean canCreateOrder() {
        return this == PET_OWNER || this == BOTH;
    }

    public boolean canAcceptOrder() {
        return this == SITTER || this == BOTH;
    }

    public static UserRoleTypeEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        UserRoleTypeEnum userRoleTypeEnum = fromCode(code);
        return userRoleTypeEnum == null ? null : userRoleTypeEnum.getDesc();
    }
}
