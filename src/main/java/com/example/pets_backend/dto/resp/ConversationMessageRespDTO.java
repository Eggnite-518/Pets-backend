package com.example.pets_backend.dto.resp;

public record ConversationMessageRespDTO(
        Long messageId,
        Integer senderRole,
        String content,
        String sentAt) {
}
