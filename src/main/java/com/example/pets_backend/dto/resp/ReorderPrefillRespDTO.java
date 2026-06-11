package com.example.pets_backend.dto.resp;

import java.util.List;

public record ReorderPrefillRespDTO(
        String sourceOrderId,
        Long addressId,
        String contactName,
        String contactPhone,
        String province,
        String city,
        String district,
        String detailAddress,
        String addressTag,
        Integer serviceType,
        List<Long> petIds,
        List<String> hardFilterTags,
        List<String> hardFilterTagDescs,
        OrderRequirementTagsRespDTO requirementTags,
        String remark) {
}
