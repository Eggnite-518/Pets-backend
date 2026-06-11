package com.example.pets_backend.dto.resp;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRespDTO(
        Long orderId,
        BigDecimal totalAmount,
        Integer status,
        String statusDesc,
        String createdAt,
        List<String> hardFilterTags,
        List<String> hardFilterTagDescs,
        OrderRequirementTagsRespDTO requirementTags) {
}
