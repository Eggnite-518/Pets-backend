package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.time.LocalDate;

public class ValidateServiceDateStep implements FulfillmentNodeStep {

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.VALIDATE_SERVICE_DATE;
    }

    @Override
    public void handle(FulfillmentContext context) {
        if (context.order().getServiceDate() == null) {
            return;
        }
        if (!LocalDate.now().equals(context.order().getServiceDate())) {
            throw new ClientException(BaseErrorCode.FULFILLMENT_TIME_ERROR);
        }
    }
}
