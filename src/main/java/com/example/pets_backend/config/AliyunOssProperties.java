package com.example.pets_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Aliyun OSS configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "pets.oss")
public class AliyunOssProperties {

    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String publicBaseUrl;
    private long signedUrlExpirationMinutes = 10;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public long getSignedUrlExpirationMinutes() {
        return signedUrlExpirationMinutes;
    }

    public void setSignedUrlExpirationMinutes(long signedUrlExpirationMinutes) {
        this.signedUrlExpirationMinutes = signedUrlExpirationMinutes;
    }
}
