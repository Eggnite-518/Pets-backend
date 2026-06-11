package com.example.pets_backend.infrastructure.oss;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.pets_backend.config.AliyunOssProperties;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AliyunOssObjectStorageService implements ObjectStorageService {

    private final AliyunOssProperties aliyunOssProperties;

    @Override
    public OssUploadResult upload(OssUploadCommand command) {
        validateConfiguration();
        validateCommand(command);
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret());
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(command.fileSize());
            if (!isBlank(command.contentType())) {
                metadata.setContentType(command.contentType());
            }
            ossClient.putObject(aliyunOssProperties.getBucketName(), command.objectKey(), command.inputStream(), metadata);
            return new OssUploadResult(command.objectKey(), buildFileUrl(command.objectKey()), command.fileSize(), command.contentType());
        } catch (OSSException | ClientException ex) {
            throw new com.example.pets_backend.frameworks.convention.exception.ClientException(BaseErrorCode.OSS_UPLOAD_ERROR);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        validateConfiguration();
        if (isBlank(objectKey)) {
            return null;
        }
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret());
        try {
            Date expiration = new Date(System.currentTimeMillis() + signedUrlExpirationMillis());
            return ossClient.generatePresignedUrl(aliyunOssProperties.getBucketName(), objectKey, expiration)
                    .toString();
        } catch (OSSException | ClientException ex) {
            throw new com.example.pets_backend.frameworks.convention.exception.ClientException(BaseErrorCode.OSS_SIGNED_URL_ERROR);
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateCommand(OssUploadCommand command) {
        if (command == null || isBlank(command.objectKey()) || command.inputStream() == null
                || command.fileSize() < 0) {
            throw new com.example.pets_backend.frameworks.convention.exception.ClientException(BaseErrorCode.OSS_UPLOAD_ERROR);
        }
    }

    private void validateConfiguration() {
        if (isBlank(aliyunOssProperties.getEndpoint()) || isBlank(aliyunOssProperties.getBucketName())
                || isBlank(aliyunOssProperties.getAccessKeyId()) || isBlank(aliyunOssProperties.getAccessKeySecret())) {
            throw new com.example.pets_backend.frameworks.convention.exception.ClientException(BaseErrorCode.OSS_CONFIGURATION_ERROR);
        }
    }

    private String buildFileUrl(String objectKey) {
        if (!isBlank(aliyunOssProperties.getPublicBaseUrl())) {
            return trimTrailingSlash(aliyunOssProperties.getPublicBaseUrl()) + "/" + objectKey;
        }
        return "https://" + aliyunOssProperties.getBucketName() + "." + normalizeEndpointForPublicUrl() + "/" + objectKey;
    }

    private String normalizeEndpointForPublicUrl() {
        String endpoint = aliyunOssProperties.getEndpoint();
        if (endpoint.startsWith("https://")) {
            return endpoint.substring("https://".length());
        }
        if (endpoint.startsWith("http://")) {
            return endpoint.substring("http://".length());
        }
        return endpoint;
    }

    private long signedUrlExpirationMillis() {
        long minutes = aliyunOssProperties.getSignedUrlExpirationMinutes();
        if (minutes <= 0) {
            minutes = 10;
        }
        return minutes * 60 * 1000;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
