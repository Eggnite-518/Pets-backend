package com.example.pets_backend.dto.resp;

public record SubmitExamRespDTO(
        Integer score,
        boolean passed,
        boolean corePassed) {
}

