package com.example.pets_backend.dto.resp;

public record OrderAlipayPaymentRespDTO(
        String paymentId,
        String orderId,
        String payChannel,
        String payUrl,
        String expiresAt) {
}
