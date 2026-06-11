package com.example.pets_backend.dto.resp;

public record UpgradeCaretakerRoleRespDTO(
        Long userId,
        Integer roleType,
        String roleTypeDesc,
        String token,
        Integer verifyStatus) {
}
