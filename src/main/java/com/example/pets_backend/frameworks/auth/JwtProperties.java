package com.example.pets_backend.frameworks.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Component
@ConfigurationProperties(prefix = "pets.jwt")
public class JwtProperties {

    /**
     * 签名密钥
     */
    private String secret = "pets-backend-default-secret-key-change-me-1234567890";

    /**
     * 签发者
     */
    private String issuer = "pets-backend";

    /**
     * 过期时间，单位：秒
     */
    private long ttlSeconds = 24 * 60 * 60;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}

