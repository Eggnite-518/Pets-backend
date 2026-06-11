package com.example.pets_backend.dto.resp;

import java.util.List;

public record CaretakerDepositRefundStatusRespDTO(
        Boolean pending,
        String applyTime,
        String expectedSettleTime,
        Long remainingSeconds,
        Boolean canSettle,
        Integer creditScore,
        String depositAmount,
        String estimatedRefundAmount,
        String estimatedPenaltyAmount,
        Boolean blocked,
        List<String> blockReasons,
        String custodyRule) {
}
