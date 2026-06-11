package com.example.pets_backend.dto.resp;

import java.time.LocalDateTime;
import java.util.List;

public record OrderRatingDetailRespDTO(
        Long reviewId,
        Long orderId,
        Integer overallScore,
        Integer punctualityScore,
        Integer professionalScore,
        String comment,
        List<ReviewDeductionReasonRespDTO> deductionReasons,
        List<Attachment> attachments,
        Integer reviewStatus,
        String reviewStatusDesc,
        Boolean canAppeal,
        LocalDateTime appealDeadline,
        LocalDateTime createdAt) {

    public record Attachment(
            String url,
            String objectKey,
            String mediaType,
            String contentType,
            Long fileSize,
            Integer sortOrder) {
    }
}
