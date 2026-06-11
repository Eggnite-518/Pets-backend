package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.FulfillmentMediaStorageService;
import com.example.pets_backend.service.FulfillmentMediaStorageService.StoredMedia;
import com.example.pets_backend.service.video.VideoProcessingStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreDirectMediaStep implements FulfillmentNodeStep {

    private static final String MEDIA_TYPE_IMAGE = "IMAGE";

    private final FulfillmentMediaStorageService fulfillmentMediaStorageService;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.STORE_DIRECT_MEDIA;
    }

    @Override
    public void handle(FulfillmentContext context) {
        if (context.node().requiresImage()
                && (context.file() == null || context.file().isEmpty())) {
            throw new ClientException(BaseErrorCode.FULFILLMENT_IMAGE_REQUIRED_ERROR);
        }
        StoredMedia storedMedia = fulfillmentMediaStorageService.storeUploadedMedia(
                context.orderId(), context.node().code(), context.file());
        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setOrderId(context.orderId());
        record.setNodeType(context.node().code());
        record.setLatitude(context.lat());
        record.setLongitude(context.lng());
        record.setMediaType(deriveMediaType(context));
        record.setObjectKey(storedMedia.objectKey());
        record.setImageUrl(storedMedia.mediaUrl());
        record.setContentType(storedMedia.contentType());
        record.setFileSize(storedMedia.fileSize());
        record.setProcessingStatus(VideoProcessingStatus.SUCCESS);
        context.record(record);
    }

    private String deriveMediaType(FulfillmentContext context) {
        if (context.file() == null || context.file().isEmpty()) {
            return null;
        }
        return MEDIA_TYPE_IMAGE;
    }
}
