package com.example.pets_backend.enums;

public enum ExceptionReportStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    RESOLVED(2, "已解决");

    private final Integer code;
    private final String desc;

    ExceptionReportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
