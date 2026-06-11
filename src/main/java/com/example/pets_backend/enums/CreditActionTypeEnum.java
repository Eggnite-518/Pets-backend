package com.example.pets_backend.enums;

import java.util.Arrays;

public enum CreditActionTypeEnum {

    FIVE_STAR(1, 4, false, "五星好评"),
    NOT_ON_TIME(2, -3, false, "未能按时履约"),
    LOW_SERVICE_QUALITY(3, -4, false, "服务质量低"),
    BAD_ATTITUDE(4, -2, false, "服务态度差"),
    LATE_CHECKIN(5, -1, false, "未能按时上传打卡"),
    SERIOUS_ACCIDENT(6, -100, true, "严重事故"),
    APPEAL_CORRECTION(7, 0, false, "评价申诉修正");

    private final Integer code;
    private final Integer delta;
    private final boolean ban;
    private final String desc;

    CreditActionTypeEnum(Integer code, Integer delta, boolean ban, String desc) {
        this.code = code;
        this.delta = delta;
        this.ban = ban;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public Integer getDelta() {
        return delta;
    }

    public boolean isBan() {
        return ban;
    }

    public String getDesc() {
        return desc;
    }

    public static CreditActionTypeEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
