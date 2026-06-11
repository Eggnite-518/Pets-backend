package com.example.pets_backend.enums;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.Arrays;

public enum EmergencyExceptionTypeEnum {

    PET_ANOMALY(2, "宠物异常"),
    PERSONAL_THREAT(6, "人身威胁");

    private final Integer code;
    private final String desc;

    EmergencyExceptionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EmergencyExceptionTypeEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ClientException(BaseErrorCode.CLIENT_ERROR));
    }

    public static String getDescByCode(Integer code) {
        EmergencyExceptionTypeEnum typeEnum = Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
        return typeEnum == null ? null : typeEnum.getDesc();
    }
}
