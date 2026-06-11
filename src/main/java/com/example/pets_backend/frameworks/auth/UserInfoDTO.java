package com.example.pets_backend.frameworks.auth;

/**
 * JWT 中传递的用户身份信息
 */
public record UserInfoDTO(
        Long userId,
        String phone,
        String nickname,
        Integer roleType,
        String token) {
}

