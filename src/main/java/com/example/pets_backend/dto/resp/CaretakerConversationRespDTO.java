package com.example.pets_backend.dto.resp;

public record CaretakerConversationRespDTO(
        String conversationId,
        String orderId,
        String peerId,
        String peerName,
        String peerAvatarUrl,
        String petName,
        String lastMessagePreview,
        String lastMessageTimeText,
        Integer unreadCount) {
}
