package com.example.pets_backend.dto.resp;

public record SubmitReviewAppealRespDTO(
        Long appealId,
        Integer appealStatus,
        String appealStatusDesc) {
}
