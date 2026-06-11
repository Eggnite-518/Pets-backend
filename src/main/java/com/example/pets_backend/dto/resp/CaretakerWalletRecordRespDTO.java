package com.example.pets_backend.dto.resp;

public record CaretakerWalletRecordRespDTO(
        String recordId,
        Integer type,
        String typeText,
        Integer direction,
        String amount,
        String description,
        String createdAt) {
}
