package com.example.pets_backend.enums;

import java.util.Arrays;

public enum OrderStatusEnum {

    PENDING_PROVIDER(1, "悬赏中"),
    PENDING_PAYMENT(2, "待支付"),
    PENDING_FULFILLMENT(3, "待履约"),
    IN_FULFILLMENT(4, "履约中"),
    PENDING_OWNER_CONFIRMATION(5, "待宠主确认"),
    COMPLETED(6, "已完成"),
    BLOCKED_WAIT_OWNER(7, "履约受阻-等待雇主确认"),
    EXCEPTION_ENDED(8, "异常结束"),
    EMERGENCY_PLATFORM_INTERVENTION(9, "紧急终止/平台介入");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        OrderStatusEnum orderStatusEnum = fromCode(code);
        return orderStatusEnum == null ? null : orderStatusEnum.getDesc();
    }
}
