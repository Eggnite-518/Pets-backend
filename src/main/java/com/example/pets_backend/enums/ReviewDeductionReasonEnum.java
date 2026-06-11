package com.example.pets_backend.enums;

import java.util.Arrays;

public enum ReviewDeductionReasonEnum {

    NOT_ON_TIME(1, "未按时到达", CreditActionTypeEnum.NOT_ON_TIME),
    NOT_FEED_AS_REQUIRED(2, "未按要求喂食", CreditActionTypeEnum.LOW_SERVICE_QUALITY),
    PET_ABNORMAL_NOT_REPORTED(3, "宠物异常未及时反馈", CreditActionTypeEnum.LOW_SERVICE_QUALITY),
    MISSING_CHECKIN(4, "打卡记录缺失", CreditActionTypeEnum.LATE_CHECKIN),
    BAD_ATTITUDE(5, "服务态度差", CreditActionTypeEnum.BAD_ATTITUDE),
    OTHER(6, "其他", CreditActionTypeEnum.LOW_SERVICE_QUALITY);

    private final Integer code;
    private final String desc;
    private final CreditActionTypeEnum creditActionType;

    ReviewDeductionReasonEnum(Integer code, String desc, CreditActionTypeEnum creditActionType) {
        this.code = code;
        this.desc = desc;
        this.creditActionType = creditActionType;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public CreditActionTypeEnum getCreditActionType() {
        return creditActionType;
    }

    public static ReviewDeductionReasonEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
