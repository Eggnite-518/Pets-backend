package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.Arrays;
import java.util.List;

public enum FulfillmentNodeType {

    ARRIVAL(1, "抵达签到", false, false, true, directSteps()),
    ENTER_HOME(2, "入户确认", false, true, true, directSteps()),
    FEED_WATER(3, "喂食换水", false, true, true, directSteps()),
    CLEAN_LITTER(4, "铲屎清洁", false, true, true, directSteps()),
    WALKING(5, "遛宠中", false, true, false, directSteps()),
    LOCK_LEAVE(6, "锁门离场", true, false, true, videoSteps());

    private final int code;
    private final String desc;
    private final boolean video;
    private final boolean imageRequired;
    private final boolean validateLocation;
    private final List<FulfillmentStepKey> steps;

    FulfillmentNodeType(
            int code,
            String desc,
            boolean video,
            boolean imageRequired,
            boolean validateLocation,
            List<FulfillmentStepKey> steps) {
        this.code = code;
        this.desc = desc;
        this.video = video;
        this.imageRequired = imageRequired;
        this.validateLocation = validateLocation;
        this.steps = steps;
    }

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }

    public boolean isVideo() {
        return video;
    }

    public boolean requiresImage() {
        return imageRequired;
    }

    public boolean shouldValidateLocation() {
        return validateLocation;
    }

    public List<FulfillmentStepKey> steps() {
        return steps;
    }

    public static FulfillmentNodeType fromCode(Integer code) {
        if (code == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return Arrays.stream(values())
                .filter(node -> node.code == code)
                .findFirst()
                .orElseThrow(() -> new ClientException(BaseErrorCode.CLIENT_ERROR));
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(node -> node.code == code)
                .map(FulfillmentNodeType::desc)
                .findFirst()
                .orElse(null);
    }

    private static List<FulfillmentStepKey> directSteps() {
        return List.of(
                FulfillmentStepKey.LOAD_ASSIGNED_ORDER,
                FulfillmentStepKey.VALIDATE_SERVICE_NODE,
                FulfillmentStepKey.VALIDATE_FULFILLMENT_STATUS,
                FulfillmentStepKey.VALIDATE_SERVICE_DATE,
                FulfillmentStepKey.VALIDATE_LOCATION,
                FulfillmentStepKey.VALIDATE_SEQUENCE,
                FulfillmentStepKey.VALIDATE_NOT_DUPLICATE,
                FulfillmentStepKey.STORE_DIRECT_MEDIA,
                FulfillmentStepKey.PERSIST_RECORD,
                FulfillmentStepKey.ADVANCE_ORDER_STATUS);
    }

    private static List<FulfillmentStepKey> videoSteps() {
        return List.of(
                FulfillmentStepKey.LOAD_ASSIGNED_ORDER,
                FulfillmentStepKey.VALIDATE_SERVICE_NODE,
                FulfillmentStepKey.VALIDATE_FULFILLMENT_STATUS,
                FulfillmentStepKey.VALIDATE_SERVICE_DATE,
                FulfillmentStepKey.VALIDATE_LOCATION,
                FulfillmentStepKey.VALIDATE_SEQUENCE,
                FulfillmentStepKey.VALIDATE_NOT_DUPLICATE,
                FulfillmentStepKey.STORE_VIDEO_SOURCE,
                FulfillmentStepKey.PERSIST_RECORD,
                FulfillmentStepKey.ADVANCE_ORDER_STATUS,
                FulfillmentStepKey.SUBMIT_VIDEO_PROCESSING);
    }
}
