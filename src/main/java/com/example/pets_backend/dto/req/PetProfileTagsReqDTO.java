package com.example.pets_backend.dto.req;

import java.math.BigDecimal;
import java.util.List;

public record PetProfileTagsReqDTO(
        BigDecimal weightKg,
        String ageGroup,
        List<String> physiologicalStates,
        Integer socialFriendliness,
        String aggressionLevel,
        List<String> outdoorBehaviors,
        List<String> indoorBehaviors,
        List<String> healthTags) {
}
