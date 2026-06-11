package com.example.pets_backend.infrastructure.oss;

import java.io.InputStream;

public record OssUploadCommand(
        String objectKey,
        InputStream inputStream,
        long fileSize,
        String contentType) {
}
