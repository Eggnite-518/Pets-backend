package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.FulfillmentRecordQuerySupport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateNotDuplicateStep implements FulfillmentNodeStep {

    private final FulfillmentRecordDao fulfillmentRecordDao;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.VALIDATE_NOT_DUPLICATE;
    }

    @Override
    public void handle(FulfillmentContext context) {
        boolean alreadyDone = FulfillmentRecordQuerySupport
                .excludingDemoSeed(fulfillmentRecordDao.selectByOrderId(context.orderId()))
                .stream()
                .filter(FulfillmentRecordQuerySupport::blocksRetry)
                .map(FulfillmentRecordDO::getNodeType)
                .anyMatch(nodeType -> nodeType.equals(context.node().code()));
        if (alreadyDone) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        fulfillmentRecordDao.deleteFailedByOrderIdAndNodeType(
                context.orderId(), context.node().code());
        fulfillmentRecordDao.deleteDemoSeedByOrderIdAndNodeType(
                context.orderId(), context.node().code());
    }
}
