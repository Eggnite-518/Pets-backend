package com.example.pets_backend.dto.resp;

public record ReviewAttachmentUploadRespDTO(
        String url,
        String objectKey,
        String mediaType,
        String contentType,
        Long fileSize) {
}
