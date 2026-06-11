package com.example.pets_backend.dto.resp;

import java.util.List;

public record CandidateListRespDTO(
        Long orderId,
        String sortBy,
        String sortByDesc,
        List<CandidateListItemRespDTO> candidates) {
}
