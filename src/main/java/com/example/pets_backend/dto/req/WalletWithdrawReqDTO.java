package com.example.pets_backend.dto.req;

import java.math.BigDecimal;

public record WalletWithdrawReqDTO(
        BigDecimal amount,
        String payeeAccount,
        String payeeRealName,
        String remark) {
}

