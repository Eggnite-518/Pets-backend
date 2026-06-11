package com.example.pets_backend.service.fulfillment;

public interface FulfillmentNodeStep {

    FulfillmentStepKey key();

    void handle(FulfillmentContext context);
}
