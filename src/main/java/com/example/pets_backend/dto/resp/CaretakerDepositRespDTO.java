package com.example.pets_backend.dto.resp;

public record CaretakerDepositRespDTO(
        String depositAmount,
        String basicRequiredAmount,
        String premiumRequiredAmount,
        Boolean basicReady,
        Boolean premiumReady,
        String walletBalance,
        String frozenAmount,
        String custodyRule) {
}
