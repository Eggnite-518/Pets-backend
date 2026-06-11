package com.example.pets_backend.dto.req;

public record SelfReportExceptionReqDTO(
        Integer exceptionType,
        String description) {
}

