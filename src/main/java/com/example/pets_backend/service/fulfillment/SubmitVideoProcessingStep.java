package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.service.video.AsyncVideoProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RequiredArgsConstructor
public class SubmitVideoProcessingStep implements FulfillmentNodeStep {

    private final AsyncVideoProcessingService asyncVideoProcessingService;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.SUBMIT_VIDEO_PROCESSING;
    }

    @Override
    public void handle(FulfillmentContext context) {
        if (context.videoProcessingContext() == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            asyncVideoProcessingService.submit(context.videoProcessingContext());
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncVideoProcessingService.submit(context.videoProcessingContext());
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    asyncVideoProcessingService.deleteTempFiles(context.videoProcessingContext());
                }
            }
        });
    }
}
