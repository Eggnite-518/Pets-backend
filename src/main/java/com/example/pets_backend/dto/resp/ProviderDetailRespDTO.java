package com.example.pets_backend.dto.resp;

import java.util.List;

public record ProviderDetailRespDTO(
        Long applicationId,
        Long orderId,
        Integer orderStatus,
        String orderStatusText,
        List<ServiceItemRespDTO> serviceItems,
        Integer totalAmount,
        Double distanceKm,
        String petName,
        String petAvatarUrl,
        String petMemo,
        Long providerId,
        String providerNickname,
        String providerAvatarUrl,
        Integer creditScore,
        Double rating,
        Integer totalOrderCount,
        Double complianceRate,
        String levelTag,
        List<String> certLabels,
        Integer reviewCount) {
}
