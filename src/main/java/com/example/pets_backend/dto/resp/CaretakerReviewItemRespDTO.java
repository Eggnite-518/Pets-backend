package com.example.pets_backend.dto.resp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CaretakerReviewItemRespDTO(
        Long reviewId,
        Long orderId,
        LocalDate serviceDate,
        List<ReviewPetBriefRespDTO> pets,
        Integer overallScore,
        Integer punctualityScore,
        Integer professionalScore,
        String comment,
        List<ReviewDeductionReasonRespDTO> deductionReasons,
        Integer creditDeductionScore,
        List<ReviewAttachmentRespDTO> attachments,
        Integer reviewStatus,
        String reviewStatusDesc,
        Boolean canAppeal,
        String appealUnavailableReason,
        LocalDateTime appealDeadline,
        LocalDateTime createdAt) {
}
