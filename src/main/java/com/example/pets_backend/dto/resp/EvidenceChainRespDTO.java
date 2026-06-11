package com.example.pets_backend.dto.resp;

import java.util.List;

public record EvidenceChainRespDTO(
                OrderMetaRespDTO order,
                List<FulfillmentRecordRespDTO> fulfillmentRecords,
                List<OfficialMessageRespDTO> officialMessages) {
}
