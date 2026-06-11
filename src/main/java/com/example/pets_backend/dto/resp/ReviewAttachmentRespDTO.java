package com.example.pets_backend.dto.resp;

public record ReviewAttachmentRespDTO(
        Long attachmentId,
        String url,
        String objectKey,
        String mediaType,
        String contentType,
        Long fileSize,
        Integer sortOrder) {
}
