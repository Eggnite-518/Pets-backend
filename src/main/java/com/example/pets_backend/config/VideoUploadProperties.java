package com.example.pets_backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Video upload validation properties.
 */
@Component
@ConfigurationProperties(prefix = "pets.video")
public class VideoUploadProperties {

    private long maxFileSizeMb = 200;
    private int maxFrameRate = 90;
    private String ffmpegPath = "ffmpeg";
    private String watermarkFontPath;
    private String watermarkTemplate = "\u5ba0\u6258\uff1a%s";
    private String tempDir;
    private String targetContentType = "video/mp4";
    private String targetExtension = "mp4";
    private List<String> allowedContentTypes = List.of("video/mp4", "video/quicktime", "video/x-msvideo",
            "video/x-matroska", "video/webm");
    private List<String> allowedExtensions = List.of("mp4", "mov", "avi", "mkv", "webm");

    public long getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(long maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }

    public int getMaxFrameRate() {
        return maxFrameRate;
    }

    public void setMaxFrameRate(int maxFrameRate) {
        this.maxFrameRate = maxFrameRate;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public String getWatermarkFontPath() {
        return watermarkFontPath;
    }

    public void setWatermarkFontPath(String watermarkFontPath) {
        this.watermarkFontPath = watermarkFontPath;
    }

    public String getWatermarkTemplate() {
        return watermarkTemplate;
    }

    public void setWatermarkTemplate(String watermarkTemplate) {
        this.watermarkTemplate = watermarkTemplate;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getTargetContentType() {
        return targetContentType;
    }

    public void setTargetContentType(String targetContentType) {
        this.targetContentType = targetContentType;
    }

    public String getTargetExtension() {
        return targetExtension;
    }

    public void setTargetExtension(String targetExtension) {
        this.targetExtension = targetExtension;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}
