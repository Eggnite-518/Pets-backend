package com.example.pets_backend.dto.resp;

public record TempCaretakerReadyRespDTO(
        Long userId,
        Integer roleType,
        String roleTypeDesc,
        String token,
        Integer verifyStatus,
        Boolean realNameVerified,
        String depositAmount,
        Boolean canApplyOrder) {
}
