package com.example.pets_backend.dto.resp;

public record CaretakerStatsRespDTO(
        int todayOrderCount,
        int creditScore,
        int pendingPaymentCount) {
}

