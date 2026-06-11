package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;

public class ValidateServiceNodeStep implements FulfillmentNodeStep {

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.VALIDATE_SERVICE_NODE;
    }

    @Override
    public void handle(FulfillmentContext context) {
        ServiceFulfillmentFlow flow = ServiceFulfillmentFlow.fromServiceType(context.order().getServiceType());
        if (!flow.contains(context.node())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }
}
