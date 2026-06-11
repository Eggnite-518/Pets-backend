package com.example.pets_backend.dto.resp;

import java.util.List;

public record AdminReviewAppealPageRespDTO(
        Long total,
        Integer page,
        Integer pageSize,
        List<ReviewAppealRespDTO> list) {
}
