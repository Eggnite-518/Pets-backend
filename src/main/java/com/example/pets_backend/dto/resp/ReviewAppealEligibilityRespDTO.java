package com.example.pets_backend.dto.resp;

import java.time.LocalDateTime;

public record ReviewAppealEligibilityRespDTO(
        Long reviewId,
        Boolean canAppeal,
        String unavailableReason,
        LocalDateTime appealDeadline) {
}
