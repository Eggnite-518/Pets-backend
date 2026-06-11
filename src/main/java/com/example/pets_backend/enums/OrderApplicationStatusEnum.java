package com.example.pets_backend.enums;

import java.util.Arrays;

public enum OrderApplicationStatusEnum {

    APPLYING(0, "报名中"),
    REJECTED(1, "落选"),
    SELECTED(2, "被选中");

    private final Integer code;
    private final String desc;

    OrderApplicationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderApplicationStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        OrderApplicationStatusEnum orderApplicationStatusEnum = fromCode(code);
        return orderApplicationStatusEnum == null ? null : orderApplicationStatusEnum.getDesc();
    }
}
