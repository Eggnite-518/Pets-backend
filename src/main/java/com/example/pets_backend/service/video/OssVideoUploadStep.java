package com.example.pets_backend.service.video;

import com.example.pets_backend.config.VideoUploadProperties;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Uploads the processed video to object storage.
 */
@Component
@Order(20)
@RequiredArgsConstructor
public class OssVideoUploadStep implements VideoProcessingStep {

    private final ObjectStorageService objectStorageService;
    private final VideoUploadProperties videoUploadProperties;

    @Override
    public void execute(VideoProcessingContext context) {
        Path processedFile = context.getProcessedFile();
        String objectKey = buildProcessedObjectKey(context);
        try (InputStream inputStream = Files.newInputStream(processedFile)) {
            OssUploadCommand command = new OssUploadCommand(objectKey, inputStream, Files.size(processedFile),
                    videoUploadProperties.getTargetContentType());
            OssUploadResult uploadResult = objectStorageService.upload(command);
            context.setProcessedObjectKey(objectKey);
            context.setUploadResult(uploadResult);
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.VIDEO_TEMP_FILE_ERROR);
        }
    }

    private String buildProcessedObjectKey(VideoProcessingContext context) {
        return "fulfillment/orders/" + context.getRecord().getOrderId()
                + "/nodes/" + context.getRecord().getNodeType()
                + "/processed/" + UUID.randomUUID() + "." + videoUploadProperties.getTargetExtension();
    }
}
