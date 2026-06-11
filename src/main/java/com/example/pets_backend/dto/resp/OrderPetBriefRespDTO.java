package com.example.pets_backend.dto.resp;

public record OrderPetBriefRespDTO(
        Long petId,
        String petName,
        Integer petType,
        String petTypeDesc) {
}
