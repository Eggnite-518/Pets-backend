package com.example.pets_backend.dto.resp;

public record ApplicationBriefRespDTO(
        Long applicationId,
        Long providerId,
        String providerNickname,
        String providerAvatarUrl,
        Integer applyStatus,
        String applyStatusDesc) {
}
