package com.example.pets_backend.dto.resp;

import java.util.List;

public record OrderDetailRespDTO(
        String orderId,
        String serviceDate,
        String totalAmount,
        String addressSnapshot,
        Integer status,
        String serviceLabel,
        List<String> hardFilterTags,
        List<String> hardFilterTagDescs,
        OrderRequirementTagsRespDTO requirementTags,
        String remark,
        List<PetBrief> pets,
        List<ApplicationBrief> applications) {

    public record PetBrief(
            String petId,
            String petName,
            Integer petType) {
    }

    public record ApplicationBrief(
            String applicationId,
            String providerId,
            String providerNickname,
            String providerAvatarUrl,
            Integer applyStatus) {
    }
}
