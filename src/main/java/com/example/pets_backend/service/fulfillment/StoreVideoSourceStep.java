package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.FulfillmentMediaStorageService;
import com.example.pets_backend.service.VideoUploadService;
import com.example.pets_backend.service.video.VideoProcessingContext;
import com.example.pets_backend.service.video.VideoProcessingStatus;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreVideoSourceStep implements FulfillmentNodeStep {

    private static final String MEDIA_TYPE_VIDEO = "VIDEO";
    private static final String DEFAULT_WATERMARK_TEMPLATE = "宠托：%s";

    private final VideoUploadService videoUploadService;
    private final FulfillmentMediaStorageService fulfillmentMediaStorageService;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.STORE_VIDEO_SOURCE;
    }

    @Override
    public void handle(FulfillmentContext context) {
        videoUploadService.validateVideo(context.file(), null);
        Path sourceFile = fulfillmentMediaStorageService.saveTempSourceFile(
                context.orderId(), context.node().code(), context.file());
        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setOrderId(context.orderId());
        record.setNodeType(context.node().code());
        record.setLatitude(context.lat());
        record.setLongitude(context.lng());
        record.setMediaType(MEDIA_TYPE_VIDEO);
        record.setOriginalObjectKey(fulfillmentMediaStorageService.buildOriginalObjectKey(
                context.orderId(), context.node().code(), context.file().getOriginalFilename()));
        record.setOriginalContentType(context.file().getContentType());
        record.setOriginalFileSize(context.file().getSize());
        record.setWatermarkText(buildWatermarkText());
        record.setProcessingStatus(VideoProcessingStatus.PROCESSING);
        context.record(record);
        context.sourceFile(sourceFile);
        context.videoProcessingContext(new VideoProcessingContext(record, sourceFile, null, record.getWatermarkText()));
    }

    private String buildWatermarkText() {
        String tag = UserContext.getNickname();
        if (tag == null || tag.isBlank()) {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
            }
            tag = String.valueOf(userId);
        }
        return String.format(DEFAULT_WATERMARK_TEMPLATE, tag);
    }
}
