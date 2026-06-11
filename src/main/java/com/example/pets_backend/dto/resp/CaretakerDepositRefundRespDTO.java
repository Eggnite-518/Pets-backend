package com.example.pets_backend.dto.resp;

public record CaretakerDepositRefundRespDTO(
        String refundedAmount,
        String penaltyAmount,
        Integer refundRatePercent,
        Integer creditScore,
        String walletBalance,
        String frozenAmount,
        String custodyRule) {
}
