package com.example.pets_backend.enums;

import java.util.Arrays;

public enum PetAggressionLevelEnum {

    LOW("LOW", "低"),
    MEDIUM("MEDIUM", "中"),
    HIGH("HIGH", "高");

    private final String code;
    private final String desc;

    PetAggressionLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PetAggressionLevelEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(String code) {
        PetAggressionLevelEnum value = fromCode(code);
        return value == null ? null : value.getDesc();
    }
}
