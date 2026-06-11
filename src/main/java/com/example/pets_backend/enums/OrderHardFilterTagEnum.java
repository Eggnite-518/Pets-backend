package com.example.pets_backend.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum OrderHardFilterTagEnum {

    FEMALE_ONLY("FEMALE_ONLY", "仅限女性", 2, null),
    ACCEPT_LARGE_DOG("ACCEPT_LARGE_DOG", "接受大型犬", null, "接受大型犬"),
    MEDICAL_FEEDING_EXPERIENCE("MEDICAL_FEEDING_EXPERIENCE", "具备医疗/喂药经验", null, "具备医疗/喂药经验");

    private final String code;
    private final String desc;
    private final Integer requiredGender;
    private final String requiredCertLabel;

    OrderHardFilterTagEnum(String code, String desc, Integer requiredGender, String requiredCertLabel) {
        this.code = code;
        this.desc = desc;
        this.requiredGender = requiredGender;
        this.requiredCertLabel = requiredCertLabel;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getRequiredGender() {
        return requiredGender;
    }

    public String getRequiredCertLabel() {
        return requiredCertLabel;
    }

    public static OrderHardFilterTagEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(String code) {
        OrderHardFilterTagEnum tag = fromCode(code);
        return tag == null ? null : tag.getDesc();
    }

    public static List<String> describeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(OrderHardFilterTagEnum::getDescByCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
