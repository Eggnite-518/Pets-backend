package com.example.pets_backend.dto.resp;

import java.math.BigDecimal;
import java.util.List;

public record PetProfileTagsRespDTO(
        BigDecimal weightKg,
        String ageGroup,
        String ageGroupDesc,
        List<String> physiologicalStates,
        List<String> physiologicalStateDescs,
        Integer socialFriendliness,
        String aggressionLevel,
        String aggressionLevelDesc,
        List<String> outdoorBehaviors,
        List<String> outdoorBehaviorDescs,
        List<String> indoorBehaviors,
        List<String> indoorBehaviorDescs,
        List<String> healthTags,
        List<String> healthTagDescs) {
}
