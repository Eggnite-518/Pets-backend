package com.example.pets_backend.dto.resp;

import java.util.List;

public record TrainingCurriculumRespDTO(
        Integer requiredMaterialCount,
        Integer completedMaterialCount,
        Integer learningProgressPercent,
        List<TrainingMaterialItemRespDTO> materials) {
}
