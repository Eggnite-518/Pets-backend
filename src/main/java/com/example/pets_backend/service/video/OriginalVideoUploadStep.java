package com.example.pets_backend.service.video;

import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Stores the untouched source video before watermark transcoding.
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class OriginalVideoUploadStep implements VideoProcessingStep {

    private final ObjectStorageService objectStorageService;

    @Override
    public void execute(VideoProcessingContext context) {
        FulfillmentRecordDO record = context.getRecord();
        try (InputStream inputStream = Files.newInputStream(context.getSourceFile())) {
            objectStorageService.upload(new OssUploadCommand(
                    record.getOriginalObjectKey(),
                    inputStream,
                    Files.size(context.getSourceFile()),
                    record.getOriginalContentType()));
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.VIDEO_TEMP_FILE_ERROR);
        }
    }
}
