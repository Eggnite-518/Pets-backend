package com.example.pets_backend.dto.req;

public record UserAddressReqDTO(
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

