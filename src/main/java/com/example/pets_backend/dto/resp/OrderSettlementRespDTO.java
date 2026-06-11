package com.example.pets_backend.dto.resp;

public record OrderSettlementRespDTO(
        Long settlementId,
        Long orderId,
        Long ownerId,
        Long providerId,
        String grossAmount,
        String commissionRate,
        String commissionAmount,
        String providerIncome,
        Integer settlementStatus,
        String settlementStatusDesc,
        String settledAt) {
}
