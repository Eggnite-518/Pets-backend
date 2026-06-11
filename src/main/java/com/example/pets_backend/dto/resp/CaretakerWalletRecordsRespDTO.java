package com.example.pets_backend.dto.resp;

import java.util.List;

public record CaretakerWalletRecordsRespDTO(
        long total,
        int page,
        int pageSize,
        List<CaretakerWalletRecordRespDTO> list) {
}
