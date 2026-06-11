package com.example.pets_backend.dto.req;

import java.math.BigDecimal;

public record WalletRechargeReqDTO(
        BigDecimal amount,
        String subject) {
}

