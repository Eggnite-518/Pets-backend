package com.example.pets_backend.dto.resp;

public record ReviewPetBriefRespDTO(
        String petName,
        Integer petType,
        String petTypeDesc) {
}
