package com.example.pets_backend.dto.resp;

public record OpenOrderPetDTO(
        String petName,
        Integer petType,
        String petTypeDesc) {
}
