package com.example.pets_backend.dto.resp;

public record ReviewDeductionReasonRespDTO(
        Integer reasonType,
        String reasonTypeDesc,
        String reasonText) {
}
