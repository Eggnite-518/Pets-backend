package com.example.pets_backend.service;

import com.example.pets_backend.config.VideoUploadProperties;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VideoUploadService {

    private static final long BYTES_PER_MB = 1024L * 1024L;

    private final VideoUploadProperties videoUploadProperties;

    public void validateVideo(MultipartFile file, Integer frameRate) {
        if (file == null || file.isEmpty()) {
            throw new ClientException(BaseErrorCode.VIDEO_FILE_REQUIRED_ERROR);
        }
        validateFileSize(file.getSize());
        validateContentType(file.getContentType(), file.getOriginalFilename());
        validateExtension(file.getOriginalFilename());
        validateFrameRate(frameRate);
    }

    public String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new ClientException(BaseErrorCode.VIDEO_EXTENSION_ERROR);
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void validateFileSize(long fileSize) {
        long maxBytes = videoUploadProperties.getMaxFileSizeMb() * BYTES_PER_MB;
        if (fileSize <= 0 || fileSize > maxBytes) {
            throw new ClientException(BaseErrorCode.VIDEO_FILE_SIZE_ERROR);
        }
    }

    private void validateContentType(String contentType, String filename) {
        if (contentType != null
                && !contentType.isBlank()
                && !"application/octet-stream".equalsIgnoreCase(contentType)
                && videoUploadProperties.getAllowedContentTypes().contains(contentType)) {
            return;
        }
        if (filename != null && !filename.isBlank()) {
            String extension = getExtension(filename);
            if (videoUploadProperties.getAllowedExtensions().contains(extension)) {
                return;
            }
        }
        throw new ClientException(BaseErrorCode.VIDEO_CONTENT_TYPE_ERROR);
    }

    private void validateExtension(String filename) {
        String extension = getExtension(filename);
        if (!videoUploadProperties.getAllowedExtensions().contains(extension)) {
            throw new ClientException(BaseErrorCode.VIDEO_EXTENSION_ERROR);
        }
    }

    private void validateFrameRate(Integer frameRate) {
        if (frameRate != null && frameRate > videoUploadProperties.getMaxFrameRate()) {
            throw new ClientException(BaseErrorCode.VIDEO_FRAME_RATE_ERROR);
        }
    }
}
