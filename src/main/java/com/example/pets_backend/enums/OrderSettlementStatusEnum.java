package com.example.pets_backend.enums;

import java.util.Arrays;

public enum OrderSettlementStatusEnum {

    ESCROWED(1, "托管中"),
    SETTLED(2, "已结算");

    private final Integer code;
    private final String desc;

    OrderSettlementStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderSettlementStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        OrderSettlementStatusEnum statusEnum = fromCode(code);
        return statusEnum == null ? null : statusEnum.getDesc();
    }
}
