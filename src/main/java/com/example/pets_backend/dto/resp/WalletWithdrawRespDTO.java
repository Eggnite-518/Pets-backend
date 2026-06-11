package com.example.pets_backend.dto.resp;

public record WalletWithdrawRespDTO(
        String outBizNo,
        String status,
        String alipayOrderId) {
}

