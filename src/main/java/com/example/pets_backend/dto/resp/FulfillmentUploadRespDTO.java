package com.example.pets_backend.dto.resp;

public record FulfillmentUploadRespDTO(
                Long recordId,
                Long orderId,
                Integer nodeType,
                String nodeTypeDesc,
                String mediaType,
                String objectKey,
                String mediaUrl,
                Long fileSize,
                String contentType,
                Integer frameRate,
                String processingStatus) {
}
