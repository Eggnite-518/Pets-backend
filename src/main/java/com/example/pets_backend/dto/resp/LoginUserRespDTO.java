package com.example.pets_backend.dto.resp;

public record LoginUserRespDTO(
                Long userId,
                String nickname,
                String phone,
                Integer roleType,
                String roleTypeDesc,
                String token) {
}
