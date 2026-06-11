package com.example.pets_backend.frameworks.convention.errorcode;

/**
 * Base error code definitions grouped by module.
 */
public enum BaseErrorCode implements IErrorCode {

    CLIENT_ERROR("A000001", "请求参数有误"),

    USER_REGISTER_ERROR("A000100", "用户注册失败"),
    USER_NAME_VERIFY_ERROR("A000110", "昵称校验失败"),
    USER_NAME_EXIST_ERROR("A000111", "昵称已存在"),
    USER_NAME_SENSITIVE_ERROR("A000112", "昵称包含敏感词"),
    USER_NAME_SPECIAL_CHARACTER_ERROR("A000113", "昵称包含非法字符"),
    PASSWORD_VERIFY_ERROR("A000120", "密码格式不符合要求"),
    PASSWORD_SHORT_ERROR("A000121", "密码长度不足"),
    PASSWORD_NOT_SET_ERROR("A000122", "账号尚未设置密码，请先设置密码"),
    PASSWORD_ALREADY_SET_ERROR("A000123", "账号已设置密码，请使用修改密码"),
    PASSWORD_OLD_ERROR("A000124", "原密码错误"),
    PHONE_VERIFY_ERROR("A000151", "手机号格式不正确"),
    PHONE_EXIST_ERROR("A000152", "该手机号已注册"),
    USER_LOGIN_ERROR("A000160", "手机号或密码错误"),

    PET_ARCHIVE_NAME_VERIFY_ERROR("A000300", "宠物名称不符合要求"),
    PET_ARCHIVE_TYPE_VERIFY_ERROR("A000301", "宠物类型不合法"),
    PET_ARCHIVE_NOT_FOUND_ERROR("A000302", "未找到对应的宠物档案"),
    PET_ARCHIVE_TAGS_INVALID_ERROR("A000303", "宠物档案标签不合法"),

    USER_ADDRESS_VERIFY_ERROR("A000400", "用户地址校验失败"),
    USER_ADDRESS_NOT_FOUND_ERROR("A000401", "用户地址不存在"),

    PET_OWNER_VERIFY_ERROR("A000500", "宠主档案校验失败"),
    PET_OWNER_NOT_FOUND_ERROR("A000501", "宠主档案不存在"),
    PET_OWNER_ALREADY_EXISTS_ERROR("A000502", "宠主档案已存在"),

    AUTH_TOKEN_MISSING_ERROR("A000600", "未携带登录令牌"),
    AUTH_TOKEN_INVALID_ERROR("A000601", "登录令牌无效"),
    AUTH_TOKEN_EXPIRED_ERROR("A000602", "登录令牌已过期"),

    ORDER_NOT_FOUND_ERROR("A000700", "订单不存在"),
    ORDER_NOT_OPEN_ERROR("A000701", "订单已不在悬赏中"),
    ORDER_SELF_APPLY_ERROR("A000702", "无法报名自己的订单"),
    ORDER_ALREADY_APPLIED_ERROR("A000703", "已报名过该订单"),
    CARETAKER_ROLE_REQUIRED_ERROR("A000704", "仅宠托师可报名订单"),
    ORDER_APPLICATION_NOT_FOUND_ERROR("A000705", "未找到该订单的报名记录"),
    ORDER_NOT_OWNER_ERROR("A000706", "无权查看该订单的信息"),
    ORDER_PAYMENT_ERROR("A000707", "当前订单无法支付"),
    ORDER_FULFILLMENT_STATUS_ERROR("A000708", "当前订单状态无法上传履约视频"),
    CARETAKER_VERIFY_REQUIRED_ERROR("A000709", "尚未通过平台考核"),
    CARETAKER_BASE_DEPOSIT_REQUIRED_ERROR("A000710", "首次接单前请先缴纳200元基础保证金"),
    CARETAKER_PREMIUM_DEPOSIT_REQUIRED_ERROR("A000711", "服务名贵犬种需补缴保证金至500元"),
    CARETAKER_DEPOSIT_BALANCE_NOT_ENOUGH_ERROR("A000712", "钱包余额不足，无法划扣保证金"),
    CARETAKER_DEPOSIT_EMPTY_ERROR("A000713", "当前无可退还保证金"),
    CARETAKER_DEPOSIT_REFUND_PENDING_ERROR("A000714", "已有保证金退还申请在冷静期中"),
    CARETAKER_DEPOSIT_REFUND_NOT_APPLIED_ERROR("A000715", "暂无待处理的保证金退还申请"),
    CARETAKER_DEPOSIT_REFUND_COOLING_ERROR("A000716", "保证金退还仍在15天冷静期"),
    CARETAKER_DEPOSIT_REFUND_BLOCKED_ERROR("A000717", "存在进行中的纠纷/投诉/理赔，暂不可退还保证金"),
    ORDER_OWNER_PROVIDER_CONFLICT_ERROR("A000718", "不能报名自己发布的订单"),
    ORDER_HARD_FILTER_NOT_MATCH_ERROR("A000719", "您不符合该订单的服务门槛"),
    ORDER_HARD_FILTER_INVALID_ERROR("A000720", "硬性门槛标签不合法"),
    ORDER_REQUIREMENT_TAG_INVALID_ERROR("A000721", "订单需求标签不合法"),

    FULFILLMENT_TIME_ERROR("A000800", "不在预约服务日期"),
    FULFILLMENT_LOCATION_ERROR("A000801", "当前位置距离服务地址过远"),
    FULFILLMENT_IMAGE_REQUIRED_ERROR("A000802", "请上传打卡照片"),
    FULFILLMENT_LOCATION_REQUIRED_ERROR("A000803", "请先开启定位并获取当前位置"),

    VIDEO_FILE_REQUIRED_ERROR("A000900", "请上传视频文件"),
    VIDEO_FILE_SIZE_ERROR("A000901", "视频文件大小超出限制"),
    VIDEO_CONTENT_TYPE_ERROR("A000902", "视频文件类型不支持"),
    VIDEO_EXTENSION_ERROR("A000903", "视频文件后缀不支持"),
    VIDEO_FRAME_RATE_ERROR("A000904", "视频帧率超出限制"),
    VIDEO_NODE_TYPE_ERROR("A000905", "履约节点类型不支持"),
    TRAINING_REALNAME_REQUIRED_ERROR("A000910", "请先完成实名认证"),
    TRAINING_LEARNING_REQUIRED_ERROR("A000911", "请先完成学习"),
    TRAINING_LEARNING_INCOMPLETE_ERROR("A000913", "请先完成全部必修课程"),
    TRAINING_MATERIAL_DURATION_ERROR("A000914", "学习时长未达标"),
    TRAINING_EXAM_SESSION_ERROR("A000915", "考试会话无效，请重新开始考试"),
    TRAINING_QUESTION_LACK_ERROR("A000912", "题库数量不足"),
    TRAINING_CARETAKER_REQUIRED_ERROR("A000920", "仅宠托师可参加培训认证"),

    SMS_RATE_LIMIT_ERROR("A001000", "验证码发送过于频繁，请稍后再试"),
    SMS_CODE_INVALID_ERROR("A001001", "验证码错误"),
    SMS_CODE_EXPIRED_ERROR("A001002", "验证码已过期，请重新获取"),

    DEACTIVATE_HAS_ONGOING_ORDER("A001200", "您有尚未完结的订单，请处理后再注销"),
    DEACTIVATE_HAS_ACTIVE_DISPUTE("A001201", "您的账户存在处理中售后纠纷，暂时无法注销"),
    DEACTIVATE_HAS_BALANCE("A001202", "您的账户内仍有未提取的资金/保证金，请先操作提现"),

    OCR_RECOGNITION_ERROR("A001300", "身份证识别失败，请重新拍摄"),

    REAL_NAME_VERIFY_ERROR("A001400", "实名认证信息校验失败"),
    REAL_NAME_ALREADY_VERIFIED("A001401", "该账号已完成实名认证"),

    SERVICE_ERROR("B000001", "系统繁忙,请稍后重试"),
    OSS_CONFIGURATION_ERROR("B000100", "OSS配置不完整"),
    OSS_UPLOAD_ERROR("B000101", "OSS上传失败"),
    OSS_SIGNED_URL_ERROR("B000102", "OSS临时访问链接生成失败"),
    VIDEO_TRANSCODE_ERROR("B000200", "视频水印处理失败"),
    VIDEO_TEMP_FILE_ERROR("B000201", "视频临时文件处理失败");

    private final String code;
    private final String message;

    BaseErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
