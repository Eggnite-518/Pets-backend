package com.example.pets_backend.enums;

import java.util.Arrays;

public enum DeleteStatusEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private final Integer code;
    private final String desc;

    DeleteStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DeleteStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        DeleteStatusEnum deleteStatusEnum = fromCode(code);
        return deleteStatusEnum == null ? null : deleteStatusEnum.getDesc();
    }
}
