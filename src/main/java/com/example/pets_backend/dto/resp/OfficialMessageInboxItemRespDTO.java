package com.example.pets_backend.dto.resp;

public record OfficialMessageInboxItemRespDTO(
        Long orderId,
        String title,
        String subtitle,
        String content,
        String createdAt,
        Integer messageCount) {
}
