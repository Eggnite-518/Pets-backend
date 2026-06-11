package com.example.pets_backend.service.fulfillment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FulfillmentStepRegistry {

    private final Map<FulfillmentStepKey, FulfillmentNodeStep> steps = new EnumMap<>(FulfillmentStepKey.class);

    public FulfillmentStepRegistry(List<FulfillmentNodeStep> nodeSteps) {
        for (FulfillmentNodeStep step : nodeSteps) {
            steps.put(step.key(), step);
        }
    }

    public FulfillmentNodeStep get(FulfillmentStepKey key) {
        FulfillmentNodeStep step = steps.get(key);
        if (step == null) {
            throw new IllegalStateException("Fulfillment step not registered: " + key);
        }
        return step;
    }
}
