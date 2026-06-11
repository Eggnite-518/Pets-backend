package com.example.pets_backend.dto.resp;

import java.util.List;

public record OrderServiceFeeQuoteRespDTO(
        String currency,
        String totalAmount,
        List<PriceItemRespDTO> priceItems) {

    public record PriceItemRespDTO(
            String itemName,
            String amount,
            Integer quantity,
            String remark) {
    }
}
