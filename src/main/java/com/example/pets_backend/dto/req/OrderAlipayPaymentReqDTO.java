package com.example.pets_backend.dto.req;

public record OrderAlipayPaymentReqDTO(
        String returnUrl,
        String quitUrl) {
}
