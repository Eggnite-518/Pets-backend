package com.example.pets_backend.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AddressFamilySopReqDTO(
        @JsonProperty(required = false) OrderRequirementTagsReqDTO requirementTags,
        @JsonProperty(required = false) List<String> hardFilterTags,
        @JsonProperty(required = false) String remark) {
}
