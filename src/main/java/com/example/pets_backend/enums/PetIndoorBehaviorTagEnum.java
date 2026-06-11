package com.example.pets_backend.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum PetIndoorBehaviorTagEnum {

    DESTRUCTIVE("DESTRUCTIVE", "易拆家"),
    HIDING("HIDING", "易躲藏");

    private final String code;
    private final String desc;

    PetIndoorBehaviorTagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PetIndoorBehaviorTagEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }

    public static List<String> describeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(PetIndoorBehaviorTagEnum::fromCode)
                .filter(Objects::nonNull)
                .map(PetIndoorBehaviorTagEnum::getDesc)
                .collect(Collectors.toList());
    }
}
