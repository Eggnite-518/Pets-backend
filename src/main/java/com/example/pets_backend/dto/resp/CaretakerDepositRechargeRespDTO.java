package com.example.pets_backend.dto.resp;

public record CaretakerDepositRechargeRespDTO(
        Integer targetLevel,
        String targetDepositAmount,
        String deductedAmount,
        String depositAmount,
        String walletBalance,
        String frozenAmount,
        Boolean alreadySatisfied,
        String custodyRule) {
}
