package com.example.pets_backend.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateOrderReqDTO(
                Long addressId,
                List<Long> petIds,
                Integer serviceType,
                LocalDate serviceDate,
                LocalTime serviceStartTime,
                LocalTime serviceEndTime,
                BigDecimal finalAmount,
                String remark,
                @JsonProperty(required = false) List<String> hardFilterTags,
                @JsonProperty(required = false) OrderRequirementTagsReqDTO requirementTags) {
}
