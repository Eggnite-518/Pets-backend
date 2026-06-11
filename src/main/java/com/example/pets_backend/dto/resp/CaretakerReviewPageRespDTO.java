package com.example.pets_backend.dto.resp;

import java.util.List;

public record CaretakerReviewPageRespDTO(
        Long total,
        Integer page,
        Integer pageSize,
        List<CaretakerReviewItemRespDTO> list) {
}
