package com.example.pets_backend.service.video;

import com.example.pets_backend.config.VideoUploadProperties;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Persists the processed video result to the fulfillment record.
 */
@Component
@Order(30)
@RequiredArgsConstructor
public class FulfillmentRecordUpdateStep implements VideoProcessingStep {

    private static final String MEDIA_TYPE_VIDEO = "VIDEO";

    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final VideoUploadProperties videoUploadProperties;

    @Override
    public void execute(VideoProcessingContext context) {
        OssUploadResult uploadResult = context.getUploadResult();
        FulfillmentRecordDO record = context.getRecord();
        record.setImageUrl(null);
        record.setMediaType(MEDIA_TYPE_VIDEO);
        record.setObjectKey(uploadResult.objectKey());
        record.setFileSize(uploadResult.fileSize());
        record.setContentType(videoUploadProperties.getTargetContentType());
        record.setFrameRate(context.getFrameRate());
        record.setWatermarkText(context.getWatermarkText());
        record.setProcessingStatus(VideoProcessingStatus.SUCCESS);
        record.setProcessingError(null);
        fulfillmentRecordDao.updateById(record);
    }
}
