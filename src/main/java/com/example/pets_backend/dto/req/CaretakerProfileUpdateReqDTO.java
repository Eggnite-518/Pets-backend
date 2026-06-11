package com.example.pets_backend.dto.req;

import java.util.List;

public record CaretakerProfileUpdateReqDTO(
        String nickname,
        String avatarUrl,
        List<String> certLabels,
        Integer serviceRangeKm,
        String residentAddress,
        Double residentLatitude,
        Double residentLongitude) {
}
