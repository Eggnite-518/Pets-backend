package com.example.pets_backend.service.video;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.frameworks.convention.exception.AbstractException;
import com.example.pets_backend.service.fulfillment.FulfillmentNodeType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Runs fulfillment video processing with a local async executor.
 */
@Service
public class AsyncVideoProcessingService {

    private static final int ORDER_STATUS_IN_FULFILL = 4;
    private static final int ORDER_STATUS_PENDING_OWNER_CONFIRMATION = 5;

    private final Executor videoTaskExecutor;
    private final VideoProcessingChain videoProcessingChain;
    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final OrderDao orderDao;

    public AsyncVideoProcessingService(@Qualifier("videoTaskExecutor") Executor videoTaskExecutor,
            VideoProcessingChain videoProcessingChain,
            FulfillmentRecordDao fulfillmentRecordDao,
            OrderDao orderDao) {
        this.videoTaskExecutor = videoTaskExecutor;
        this.videoProcessingChain = videoProcessingChain;
        this.fulfillmentRecordDao = fulfillmentRecordDao;
        this.orderDao = orderDao;
    }

    /**
     * Submits a local async video processing task.
     *
     * @param context video processing context
     */
    public void submit(VideoProcessingContext context) {
        videoTaskExecutor.execute(() -> process(context));
    }

    private void process(VideoProcessingContext context) {
        try {
            videoProcessingChain.process(context);
        } catch (RuntimeException ex) {
            markFailed(context.getRecord(), ex);
        } finally {
            deleteTempFiles(context);
        }
    }

    public void deleteTempFiles(VideoProcessingContext context) {
        deleteIfExists(context.getSourceFile());
        deleteIfExists(context.getProcessedFile());
    }

    private void markFailed(FulfillmentRecordDO record, RuntimeException ex) {
        record.setProcessingStatus(VideoProcessingStatus.FAILED);
        record.setProcessingErrorCode(resolveErrorCode(ex));
        record.setProcessingError(buildErrorMessage(ex));
        fulfillmentRecordDao.updateById(record);
        revertOrderStatusIfFinalVideoFailed(record);
    }

    private void revertOrderStatusIfFinalVideoFailed(FulfillmentRecordDO record) {
        if (record.getNodeType() == null
                || FulfillmentNodeType.LOCK_LEAVE.code() != record.getNodeType()) {
            return;
        }
        OrderDO order = orderDao.selectById(record.getOrderId());
        if (order != null && ORDER_STATUS_PENDING_OWNER_CONFIRMATION == order.getStatus()) {
            orderDao.updateStatus(record.getOrderId(), ORDER_STATUS_IN_FULFILL);
        }
    }

    private String resolveErrorCode(RuntimeException ex) {
        if (ex instanceof AbstractException abstractException) {
            return abstractException.getErrorCode();
        }
        return null;
    }

    private String buildErrorMessage(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }
}
