package com.example.pets_backend.service.encoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * 密码加密处理器
 */
@Component
public class PasswordEncoder {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * 对原始密码进行摘要加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encrypt(String rawPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digestBytes = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digestBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("password encrypt algorithm not found", exception);
        }
    }
}
