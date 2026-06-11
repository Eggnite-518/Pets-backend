package com.example.pets_backend.dto.resp;

public record PetOwnerRespDTO(
        Long ownerId,
        String nickname,
        String avatarUrl,
        String phone) {
}
