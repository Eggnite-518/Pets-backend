package com.example.pets_backend.dto.resp;

import java.util.List;

public record CaretakerOrderDetailRespDTO(
        String orderId,
        Integer orderStatus,
        String orderStatusText,
        List<ServiceItemRespDTO> serviceItems,
        String serviceDate,
        String serviceTimeSlot,
        String totalAmount,
        AddressDTO address,
        OwnerDTO owner,
        List<PetDTO> pets,
        String serviceNotes,
        OrderRequirementTagsRespDTO requirementTags,
        List<Integer> completedNodeTypes,
        List<Integer> checklistNodeTypes,
        String createdAt,
        Double distanceKm,
        Boolean hasApplied) {

    public record AddressDTO(
            String fullAddress,
            String district,
            Double lat,
            Double lng) {
    }

    public record OwnerDTO(
            String nickname,
            String avatarUrl,
            String phone) {
    }

    public record PetDTO(
            String petId,
            String petName,
            Integer petType,
            String petTypeText,
            String breed,
            String ageText,
            String avatarUrl,
            String careNotes,
            PetProfileTagsRespDTO profileTags) {
    }
}
