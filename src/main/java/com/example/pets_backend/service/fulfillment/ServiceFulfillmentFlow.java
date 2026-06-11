package com.example.pets_backend.service.fulfillment;

import java.util.Arrays;
import java.util.List;

public enum ServiceFulfillmentFlow {

    FEEDING(1, List.of(
            FulfillmentNodeType.ARRIVAL,
            FulfillmentNodeType.ENTER_HOME,
            FulfillmentNodeType.FEED_WATER,
            FulfillmentNodeType.CLEAN_LITTER,
            FulfillmentNodeType.LOCK_LEAVE)),

    WALKING(2, List.of(
            FulfillmentNodeType.ARRIVAL,
            FulfillmentNodeType.ENTER_HOME,
            FulfillmentNodeType.WALKING,
            FulfillmentNodeType.LOCK_LEAVE));

    private final int serviceType;
    private final List<FulfillmentNodeType> nodes;

    ServiceFulfillmentFlow(int serviceType, List<FulfillmentNodeType> nodes) {
        this.serviceType = serviceType;
        this.nodes = nodes;
    }

    public List<FulfillmentNodeType> nodes() {
        return nodes;
    }

    public List<Integer> nodeCodes() {
        return nodes.stream().map(FulfillmentNodeType::code).toList();
    }

    public boolean contains(FulfillmentNodeType node) {
        return nodes.contains(node);
    }

    public int indexOf(FulfillmentNodeType node) {
        return nodes.indexOf(node);
    }

    public static ServiceFulfillmentFlow fromServiceType(Integer serviceType) {
        if (serviceType == null) {
            return FEEDING;
        }
        return Arrays.stream(values())
                .filter(flow -> flow.serviceType == serviceType)
                .findFirst()
                .orElse(FEEDING);
    }
}
