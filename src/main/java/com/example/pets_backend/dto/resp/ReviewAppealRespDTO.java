package com.example.pets_backend.dto.resp;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewAppealRespDTO(
        Long appealId,
        Long reviewId,
        Long orderId,
        Long providerId,
        Long ownerId,
        String reason,
        List<String> evidenceUrls,
        Integer appealStatus,
        String appealStatusDesc,
        String adminMemo,
        LocalDateTime appealDeadline,
        LocalDateTime closedAt,
        LocalDateTime createdAt) {
}
