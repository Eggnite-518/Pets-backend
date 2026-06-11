package com.example.pets_backend.dto.req;

public record RegisterUserReqDTO(
        String nickname,
        String phone,
        String password) {
}
