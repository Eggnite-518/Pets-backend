package com.example.pets_backend.dto.resp;

import java.util.List;

public record AddressFamilySopRespDTO(
        Long addressId,
        boolean hasSop,
        OrderRequirementTagsRespDTO requirementTags,
        List<String> hardFilterTags,
        List<String> hardFilterTagDescs,
        String remark,
        String updatedAt) {
}
