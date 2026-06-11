package com.example.pets_backend.enums;

import java.util.Arrays;

public enum PetTypeEnum {

    CAT(1, "猫"),
    DOG(2, "狗"),
    EXOTIC(3, "异宠");

    private final Integer code;
    private final String desc;

    PetTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static boolean contains(Integer code) {
        return Arrays.stream(values()).anyMatch(item -> item.code.equals(code));
    }

    public static PetTypeEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        PetTypeEnum petTypeEnum = fromCode(code);
        return petTypeEnum == null ? null : petTypeEnum.getDesc();
    }
}
