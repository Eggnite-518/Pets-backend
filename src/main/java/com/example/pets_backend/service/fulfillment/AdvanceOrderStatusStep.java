package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.service.support.FulfillmentCompletionSupport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdvanceOrderStatusStep implements FulfillmentNodeStep {

    private static final int ORDER_STATUS_PENDING_FULFILL = 3;
    private static final int ORDER_STATUS_IN_FULFILL = 4;
    private static final int ORDER_STATUS_PENDING_OWNER_CONFIRMATION = 5;

    private final OrderDao orderDao;
    private final FulfillmentRecordDao fulfillmentRecordDao;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.ADVANCE_ORDER_STATUS;
    }

    @Override
    public void handle(FulfillmentContext context) {
        if (FulfillmentNodeType.ARRIVAL == context.node()
                && ORDER_STATUS_PENDING_FULFILL == context.order().getStatus()) {
            orderDao.updateStatus(context.orderId(), ORDER_STATUS_IN_FULFILL);
            return;
        }
        if (ORDER_STATUS_IN_FULFILL == context.order().getStatus()
                && FulfillmentCompletionSupport.areAllChecklistNodesUploaded(
                        context.order(), fulfillmentRecordDao)) {
            orderDao.updateStatus(context.orderId(), ORDER_STATUS_PENDING_OWNER_CONFIRMATION);
        }
    }
}
