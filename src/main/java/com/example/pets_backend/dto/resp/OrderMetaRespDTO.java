package com.example.pets_backend.dto.resp;

public record OrderMetaRespDTO(
        Long orderId,
        Long ownerId,
        Long providerId,
        Integer orderStatus,
        String orderStatusDesc) {
}
