package com.example.pets_backend.dto.resp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MyRewardingOrderRespDTO(
        Long orderId,
        LocalDate serviceDate,
        BigDecimal totalAmount,
        String addressSnapshot,
        Integer status,
        String statusDesc,
        List<String> hardFilterTags,
        List<String> hardFilterTagDescs,
        List<OrderPetBriefRespDTO> pets,
        List<ApplicationBriefRespDTO> applications) {
}
