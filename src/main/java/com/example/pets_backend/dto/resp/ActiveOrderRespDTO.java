package com.example.pets_backend.dto.resp;

public record ActiveOrderRespDTO(
        String orderId,
        Integer orderStatus,
        String orderStatusText,
        String serviceTypeText,
        String serviceDate,
        String serviceTimeSlot,
        String addressSnapshot,
        String petName,
        String petAvatarUrl) {
}
