package com.example.pets_backend.enums;

import java.util.Arrays;

public enum TrainingResetReasonEnum {

    INACTIVE_180_DAYS("INACTIVE_180_DAYS", "长期未接单"),
    COMPLAINT_OPERATION("COMPLAINT_OPERATION", "服务操作不规范被投诉");

    private final String code;
    private final String desc;

    TrainingResetReasonEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String describe(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .map(TrainingResetReasonEnum::getDesc)
                .findFirst()
                .orElse(code);
    }
}
