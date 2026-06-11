package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PersistRecordStep implements FulfillmentNodeStep {

    private final FulfillmentRecordDao fulfillmentRecordDao;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.PERSIST_RECORD;
    }

    @Override
    public void handle(FulfillmentContext context) {
        fulfillmentRecordDao.insert(context.record());
    }
}
