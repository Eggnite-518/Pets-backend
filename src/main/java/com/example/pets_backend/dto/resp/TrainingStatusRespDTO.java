package com.example.pets_backend.dto.resp;

import java.time.LocalDateTime;

public record TrainingStatusRespDTO(
        Integer verifyStatus,
        boolean realNameVerified,
        LocalDateTime learningCompletedAt,
        Integer lastExamScore,
        Boolean lastExamPassed,
        LocalDateTime lastExamAt,
        String resetReason,
        Integer requiredMaterialCount,
        Integer completedMaterialCount,
        Integer learningProgressPercent) {
}
