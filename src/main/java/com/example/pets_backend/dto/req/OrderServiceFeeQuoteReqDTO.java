package com.example.pets_backend.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrderServiceFeeQuoteReqDTO(
        List<Long> petIds,
        Integer serviceType,
        Long addressId,
        LocalDate serviceDate,
        LocalTime serviceStartTime,
        LocalTime serviceEndTime,
        String remark,
        @JsonProperty(required = false) List<String> hardFilterTags,
        @JsonProperty(required = false) OrderRequirementTagsReqDTO requirementTags) {
}
