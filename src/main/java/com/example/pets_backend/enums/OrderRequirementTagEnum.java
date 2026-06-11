package com.example.pets_backend.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum OrderRequirementTagEnum {

    FOOD_BOWL_LOCATION("FOOD_BOWL_LOCATION", "食盆位置", "ITEM_GUIDE"),
    MAIN_FOOD_STORAGE("MAIN_FOOD_STORAGE", "主粮存放处", "ITEM_GUIDE"),
    WATER_BOWL_LOCATION("WATER_BOWL_LOCATION", "饮水位置", "ITEM_GUIDE"),
    LITTER_BOX_LOCATION("LITTER_BOX_LOCATION", "猫砂盆位置", "ITEM_GUIDE"),
    LEASH_HARNESS("LEASH_HARNESS", "牵引绳/胸背", "ITEM_GUIDE"),
    PET_TREATS("PET_TREATS", "零食/营养品", "ITEM_GUIDE"),
    CLEANING_SUPPLIES("CLEANING_SUPPLIES", "清洁用品", "ITEM_GUIDE"),
    MEDICINE_SUPPLIES("MEDICINE_SUPPLIES", "药品/喂药", "ITEM_GUIDE"),

    ACCESS_CODE_LOCK("ACCESS_CODE_LOCK", "密码锁/门禁码", "ENVIRONMENT"),
    ACCESS_KEY_CUSTODY("ACCESS_KEY_CUSTODY", "钥匙托管", "ENVIRONMENT"),
    ACCESS_PROPERTY("ACCESS_PROPERTY", "物业/前台协助", "ENVIRONMENT"),
    ACCESS_INTERCOM("ACCESS_INTERCOM", "对讲开门", "ENVIRONMENT"),
    ACCESS_DOORBELL("ACCESS_DOORBELL", "按门铃联系", "ENVIRONMENT"),

    VIDEO_ENTRY_CHECKIN("VIDEO_ENTRY_CHECKIN", "入户视频打卡", "VIDEO_CHECKIN"),
    VIDEO_EXIT_CHECKIN("VIDEO_EXIT_CHECKIN", "离户视频打卡", "VIDEO_CHECKIN"),
    PHOTO_FEED_WATER("PHOTO_FEED_WATER", "添粮饮水拍照", "VIDEO_CHECKIN"),
    PHOTO_CLEAN_ARCHIVE("PHOTO_CLEAN_ARCHIVE", "清理留档拍照", "VIDEO_CHECKIN"),

    NEED_PLAY_COMPANION("NEED_PLAY_COMPANION", "需要陪玩", "SERVICE_OPTION"),
    NEED_CLEANING("NEED_CLEANING", "需要清洁", "SERVICE_OPTION");

    private final String code;
    private final String desc;
    private final String category;

    OrderRequirementTagEnum(String code, String desc, String category) {
        this.code = code;
        this.desc = desc;
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getCategory() {
        return category;
    }

    public static OrderRequirementTagEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(String code) {
        OrderRequirementTagEnum tag = fromCode(code);
        return tag == null ? null : tag.getDesc();
    }

    public static List<String> describeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(OrderRequirementTagEnum::getDescByCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
