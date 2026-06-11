package com.example.pets_backend.dto.resp;

public record CandidateListItemRespDTO(
        Long applicationId,
        Long providerId,
        String providerNickname,
        String providerAvatarUrl,
        Integer applyStatus,
        String applyStatusDesc,
        Double distanceKm,
        Double rating,
        Integer totalOrderCount,
        Integer creditScore) {
}
