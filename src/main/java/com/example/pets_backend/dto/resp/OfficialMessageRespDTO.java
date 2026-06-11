package com.example.pets_backend.dto.resp;

public record OfficialMessageRespDTO(
        Long messageId,
        Long orderId,
        Long senderId,
        Long receiverId,
        String content,
        String createdAt) {
}
