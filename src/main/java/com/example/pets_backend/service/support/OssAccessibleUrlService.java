package com.example.pets_backend.service.support;

import com.example.pets_backend.config.AliyunOssProperties;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 将数据库中存储的头像/图片地址转换为客户端可访问的 URL。
 * OSS 私有 Bucket 需返回预签名 URL；外部 URL 原样返回。
 */
@Service
@RequiredArgsConstructor
public class OssAccessibleUrlService {

    private final ObjectStorageService objectStorageService;
    private final AliyunOssProperties aliyunOssProperties;

    /** 供前端展示：OSS 地址转为预签名 URL */
    public String toDisplayUrl(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        String objectKey = extractObjectKey(storedValue);
        if (objectKey == null) {
            return storedValue;
        }
        return objectStorageService.generatePresignedUrl(objectKey);
    }

    /** 入库前规范化：去掉预签名参数，统一存 objectKey */
    public String normalizeForStorage(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        int queryIndex = trimmed.indexOf('?');
        if (queryIndex > 0) {
            trimmed = trimmed.substring(0, queryIndex);
        }
        String objectKey = extractObjectKey(trimmed);
        return objectKey != null ? objectKey : trimmed;
    }

    public String extractObjectKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            String publicBase = aliyunOssProperties.getPublicBaseUrl();
            if (publicBase != null && !publicBase.isBlank()) {
                String prefix = trimTrailingSlash(publicBase) + "/";
                if (trimmed.startsWith(prefix)) {
                    return trimmed.substring(prefix.length());
                }
            }
            String bucket = aliyunOssProperties.getBucketName();
            String endpoint = normalizeEndpoint(aliyunOssProperties.getEndpoint());
            if (bucket != null && !bucket.isBlank() && endpoint != null && !endpoint.isBlank()) {
                String hostPrefix = "https://" + bucket + "." + endpoint + "/";
                if (trimmed.startsWith(hostPrefix)) {
                    return trimmed.substring(hostPrefix.length());
                }
                hostPrefix = "http://" + bucket + "." + endpoint + "/";
                if (trimmed.startsWith(hostPrefix)) {
                    return trimmed.substring(hostPrefix.length());
                }
            }
            return null;
        }
        if (trimmed.startsWith("images/") || trimmed.startsWith("fulfillment/")
                || trimmed.startsWith("reviews/") || trimmed.startsWith("ocr/")) {
            return trimmed;
        }
        return null;
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        if (endpoint.startsWith("https://")) {
            return endpoint.substring("https://".length());
        }
        if (endpoint.startsWith("http://")) {
            return endpoint.substring("http://".length());
        }
        return endpoint;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
