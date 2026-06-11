package com.example.pets_backend.dto.resp;

public record AssignOrderProviderRespDTO(
                Long orderId,
                Long providerId,
                Integer orderStatus,
                String orderStatusDesc) {
}
