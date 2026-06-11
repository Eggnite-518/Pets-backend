package com.example.pets_backend.dto.resp;

public record OrderAlipayAppPaymentRespDTO(
        String paymentId,
        String orderId,
        String payChannel,
        String orderStr,
        String expiresAt) {
}
