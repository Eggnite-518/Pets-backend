package com.example.pets_backend.dto.resp;

public record UserAddressRespDTO(
        Long addressId,
        Long userId,
        String contactName,
        String contactPhone,
        String province,
        String city,
        String district,
        String detailAddress,
        String addressTag,
        Integer isDefault,
        Double latitude,
        Double longitude) {
}

