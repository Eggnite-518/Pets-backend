package com.example.pets_backend.dto.resp;

import java.util.List;

public record OrderRequirementTagsRespDTO(
        List<String> tags,
        List<String> tagDescs,
        String accessNote,
        String emergencyContactName,
        String emergencyContactPhone) {
}
