package com.example.pets_backend.dto.resp;

public record MyApplicationRespDTO(
        String applicationId,
        String orderId,
        Integer orderStatus,
        String orderStatusText,
        Integer serviceType,
        String serviceTypeText,
        Integer totalAmount,
        Double distanceKm,
        String serviceDate,
        String serviceTimeSlot,
        String addressSnapshot,
        String petName,
        String petAvatarUrl) {
}
