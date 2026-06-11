package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateSequenceStep implements FulfillmentNodeStep {

    private final FulfillmentRecordDao fulfillmentRecordDao;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.VALIDATE_SEQUENCE;
    }

    @Override
    public void handle(FulfillmentContext context) {
        ServiceFulfillmentFlow flow = ServiceFulfillmentFlow.fromServiceType(context.order().getServiceType());
        int targetIndex = flow.indexOf(context.node());
        if (targetIndex <= 0) {
            return;
        }
        Set<Integer> completedNodes = fulfillmentRecordDao.selectByOrderId(context.orderId()).stream()
                .map(FulfillmentRecordDO::getNodeType)
                .collect(Collectors.toSet());
        for (FulfillmentNodeType prerequisite : flow.nodes().subList(0, targetIndex)) {
            if (!completedNodes.contains(prerequisite.code())) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
        }
    }
}
