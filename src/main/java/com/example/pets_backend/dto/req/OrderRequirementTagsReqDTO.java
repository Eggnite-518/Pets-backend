package com.example.pets_backend.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OrderRequirementTagsReqDTO(
        @JsonProperty(required = false) List<String> tags,
        @JsonProperty(required = false) String accessNote,
        @JsonProperty(required = false) String emergencyContactName,
        @JsonProperty(required = false) String emergencyContactPhone) {
}
