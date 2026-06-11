package com.example.pets_backend.dto.req;

public record ResetPasswordReqDTO(String phone, String code, String newPassword) {
}
