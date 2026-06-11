package com.example.pets_backend.dto.resp;

import java.util.List;

public record CaretakerProfileRespDTO(
        String nickname,
        String avatarUrl,
        Integer gender,
        Double rating,
        String levelTag,
        List<String> certTags,
        List<String> certLabels,
        Integer serviceRangeKm,
        String residentAddress,
        Integer reviewCount) {
}
