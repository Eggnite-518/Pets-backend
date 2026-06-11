package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;

public class ValidateFulfillmentStatusStep implements FulfillmentNodeStep {

    private static final int ORDER_STATUS_PENDING_FULFILL = 3;
    private static final int ORDER_STATUS_IN_FULFILL = 4;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.VALIDATE_FULFILLMENT_STATUS;
    }

    @Override
    public void handle(FulfillmentContext context) {
        Integer status = context.order().getStatus();
        if (ORDER_STATUS_PENDING_FULFILL != status && ORDER_STATUS_IN_FULFILL != status) {
            throw new ClientException(BaseErrorCode.ORDER_FULFILLMENT_STATUS_ERROR);
        }
        if (context.node().isVideo() && ORDER_STATUS_IN_FULFILL != status) {
            throw new ClientException(BaseErrorCode.ORDER_FULFILLMENT_STATUS_ERROR);
        }
    }
}
