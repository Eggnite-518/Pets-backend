package com.example.pets_backend.dto.resp;

import java.util.List;

public record DisputeRespDTO(
        Long disputeId,
        Long orderId,
        Long plaintiffId,
        Long defendantId,
        Integer disputeType,
        String disputeTypeDesc,
        String reason,
        List<String> evidenceUrls,
        Integer disputeStatus,
        String disputeStatusDesc,
        Integer resultType,
        String resultTypeDesc,
        String adminMemo,
        String createdAt,
        String closedAt) {
}
