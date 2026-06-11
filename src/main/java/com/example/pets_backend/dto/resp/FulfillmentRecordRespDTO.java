package com.example.pets_backend.dto.resp;

public record FulfillmentRecordRespDTO(
        Integer nodeType,
        String nodeTypeDesc,
        String imageUrl,
        String mediaType,
        String objectKey,
        Long fileSize,
        String contentType,
        Integer frameRate,
        String processingStatus,
        String processingErrorCode,
        String processingError,
        String watermarkText,
        String createdAt) {
}
