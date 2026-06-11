package com.example.pets_backend.dto.resp;

import java.util.List;

public record OpenOrderRespDTO(
        Long orderId,
        List<ServiceItemRespDTO> serviceItems,
        Integer totalAmount,
        String serviceDate,
        String serviceTimeSlot,
        String addressDistrict,
        Double distanceKm,
        Integer applicationCount,
        Boolean isHot,
        Boolean hasApplied,
        List<OpenOrderPetDTO> pets,
        String createdAt,
        List<String> hardFilterTags,
        List<String> hardFilterTagDescs,
        String ownerNickname,
        String ownerAvatarUrl) {
}
