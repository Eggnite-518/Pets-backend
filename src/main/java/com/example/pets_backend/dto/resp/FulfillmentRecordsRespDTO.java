package com.example.pets_backend.dto.resp;

import java.util.List;

public record FulfillmentRecordsRespDTO(
        List<Integer> checklistNodeTypes,
        List<FulfillmentRecordRespDTO> records,
        boolean allNodesCompleted) {
}
