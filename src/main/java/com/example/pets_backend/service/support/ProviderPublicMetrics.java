package com.example.pets_backend.service.support;

import java.util.List;

public record ProviderPublicMetrics(
        int creditScore,
        double rating,
        int reviewCount,
        int totalOrderCount,
        double complianceRate,
        String levelTag,
        List<String> certLabels) {
}
