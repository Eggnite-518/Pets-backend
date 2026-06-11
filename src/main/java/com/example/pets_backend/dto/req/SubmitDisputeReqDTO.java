package com.example.pets_backend.dto.req;

import java.util.List;

public record SubmitDisputeReqDTO(
        Integer disputeType,
        String reason,
        List<String> evidenceUrls) {
}
