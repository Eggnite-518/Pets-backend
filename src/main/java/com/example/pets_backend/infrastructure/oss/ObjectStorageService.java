package com.example.pets_backend.infrastructure.oss;

public interface ObjectStorageService {

    OssUploadResult upload(OssUploadCommand command);

    String generatePresignedUrl(String objectKey);
}
