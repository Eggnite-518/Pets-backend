package com.example.pets_backend.enums;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.Arrays;

public enum CandidateSortByEnum {

    DISTANCE("distance", "距离最近"),
    RATING("rating", "评分最高"),
    TOTAL_ORDERS("totalOrders", "历史总单数");

    private final String code;
    private final String desc;

    CandidateSortByEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CandidateSortByEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return DISTANCE;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new ClientException(BaseErrorCode.CLIENT_ERROR));
    }
}
