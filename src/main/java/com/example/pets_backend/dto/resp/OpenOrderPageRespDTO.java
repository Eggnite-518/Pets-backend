package com.example.pets_backend.dto.resp;

import java.util.List;

public record OpenOrderPageRespDTO(
        long total,
        int page,
        int pageSize,
        List<OpenOrderRespDTO> list) {
}
