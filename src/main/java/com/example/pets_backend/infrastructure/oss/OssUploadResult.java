package com.example.pets_backend.infrastructure.oss;

public record OssUploadResult(
        String objectKey,
        String fileUrl,
        long fileSize,
        String contentType) {
}
