package com.example.pets_backend.enums;

import java.util.Arrays;

public enum PetAgeGroupEnum {

    INFANT("INFANT", "幼年期"),
    JUVENILE("JUVENILE", "青年期"),
    ADULT("ADULT", "成年期"),
    SENIOR("SENIOR", "老年期");

    private final String code;
    private final String desc;

    PetAgeGroupEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PetAgeGroupEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(String code) {
        PetAgeGroupEnum value = fromCode(code);
        return value == null ? null : value.getDesc();
    }
}
