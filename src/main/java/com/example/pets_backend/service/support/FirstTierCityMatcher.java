package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.entity.UserAddressDO;
import java.util.List;

public final class FirstTierCityMatcher {

    private static final List<String> KEYWORDS = List.of("北京", "上海", "广州", "深圳");

    private FirstTierCityMatcher() {
    }

    public static boolean matches(UserAddressDO address) {
        if (address == null) {
            return false;
        }
        return matchesAny(
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetailAddress());
    }

    public static boolean matchesAny(String... locationTexts) {
        if (locationTexts == null) {
            return false;
        }
        for (String text : locationTexts) {
            if (containsKeyword(text)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsKeyword(String locationText) {
        String normalized = normalize(locationText);
        if (normalized.isEmpty()) {
            return false;
        }
        for (String keyword : KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String locationText) {
        if (locationText == null) {
            return "";
        }
        String trimmed = locationText.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed)) {
            return "";
        }
        return trimmed;
    }
}
