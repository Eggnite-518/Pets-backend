package com.example.pets_backend.dto.resp;

public record IdCardOcrRespDTO(
        String realName,
        String idCardNo,
        String birthDate,
        String gender,
        String nationality,
        String address) {
}
