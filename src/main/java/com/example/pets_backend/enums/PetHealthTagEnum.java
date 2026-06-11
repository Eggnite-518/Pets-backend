package com.example.pets_backend.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum PetHealthTagEnum {

    MEDICATION("MEDICATION", "用药需求"),
    ALLERGY("ALLERGY", "过敏史"),
    DISABILITY("DISABILITY", "肢体残疾");

    private final String code;
    private final String desc;

    PetHealthTagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PetHealthTagEnum fromCode(String code) {
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
                .map(PetHealthTagEnum::fromCode)
                .filter(Objects::nonNull)
                .map(PetHealthTagEnum::getDesc)
                .collect(Collectors.toList());
    }
}
