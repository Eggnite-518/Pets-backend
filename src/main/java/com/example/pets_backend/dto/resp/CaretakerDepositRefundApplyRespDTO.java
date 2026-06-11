package com.example.pets_backend.dto.resp;

public record CaretakerDepositRefundApplyRespDTO(
        String applyTime,
        String expectedSettleTime,
        Integer coolingDays,
        String depositAmount,
        String estimatedRefundAmount,
        String estimatedPenaltyAmount,
        String custodyRule) {
}
