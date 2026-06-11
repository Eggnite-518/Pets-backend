package com.example.pets_backend.service.video;

import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import java.nio.file.Path;

public class VideoProcessingContext {

    private final FulfillmentRecordDO record;
    private final Path sourceFile;
    private final Integer frameRate;
    private final String watermarkText;
    private Path processedFile;
    private String processedObjectKey;
    private OssUploadResult uploadResult;

    public VideoProcessingContext(FulfillmentRecordDO record, Path sourceFile, Integer frameRate, String watermarkText) {
        this.record = record;
        this.sourceFile = sourceFile;
        this.frameRate = frameRate;
        this.watermarkText = watermarkText;
    }

    public FulfillmentRecordDO getRecord() {
        return record;
    }

    public Path getSourceFile() {
        return sourceFile;
    }

    public Integer getFrameRate() {
        return frameRate;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public Path getProcessedFile() {
        return processedFile;
    }

    public void setProcessedFile(Path processedFile) {
        this.processedFile = processedFile;
    }

    public String getProcessedObjectKey() {
        return processedObjectKey;
    }

    public void setProcessedObjectKey(String processedObjectKey) {
        this.processedObjectKey = processedObjectKey;
    }

    public OssUploadResult getUploadResult() {
        return uploadResult;
    }

    public void setUploadResult(OssUploadResult uploadResult) {
        this.uploadResult = uploadResult;
    }
}
