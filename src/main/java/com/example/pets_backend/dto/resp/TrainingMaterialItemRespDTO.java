package com.example.pets_backend.dto.resp;

public record TrainingMaterialItemRespDTO(
        Long materialId,
        String title,
        String content,
        String materialType,
        String moduleCode,
        Integer sortOrder,
        Integer minDurationSeconds,
        String mediaUrl,
        Integer watchedSeconds,
        Boolean completed) {
}
