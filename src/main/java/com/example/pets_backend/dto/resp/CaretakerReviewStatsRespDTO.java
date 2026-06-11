package com.example.pets_backend.dto.resp;

import java.math.BigDecimal;

public record CaretakerReviewStatsRespDTO(
        Long reviewCount,
        BigDecimal overallAvg,
        BigDecimal punctualityAvg,
        BigDecimal professionalAvg,
        Long lowScoreCount,
        BigDecimal lowScoreRate,
        Long recent30DayReviewCount,
        Long recent30DayLowScoreCount) {
}
