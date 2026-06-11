package com.example.pets_backend.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum PetOutdoorBehaviorTagEnum {

    BOLT_PULL("BOLT_PULL", "易爆冲"),
    EATS_GRASS("EATS_GRASS", "食草");

    private final String code;
    private final String desc;

    PetOutdoorBehaviorTagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PetOutdoorBehaviorTagEnum fromCode(String code) {
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
                .map(PetOutdoorBehaviorTagEnum::fromCode)
                .filter(Objects::nonNull)
                .map(PetOutdoorBehaviorTagEnum::getDesc)
                .collect(Collectors.toList());
    }
}
