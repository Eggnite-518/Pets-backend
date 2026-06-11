package com.example.pets_backend.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PetArchiveReqDTO(
        String petName,
        Integer petType,
        String defaultReq,
        String image,
        @JsonProperty(required = false) PetProfileTagsReqDTO profileTags) {
}

