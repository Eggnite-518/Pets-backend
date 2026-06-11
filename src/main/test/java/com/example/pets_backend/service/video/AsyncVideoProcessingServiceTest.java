package com.example.pets_backend.service.video;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsyncVideoProcessingServiceTest {

    @Mock
    private VideoProcessingChain videoProcessingChain;

    @Mock
    private FulfillmentRecordDao fulfillmentRecordDao;

    @Mock
    private OrderDao orderDao;

    @TempDir
    private Path tempDir;

    @Test
    void submitStoresProcessingErrorCodeWhenChainThrowsBusinessException() {
        AsyncVideoProcessingService service = new AsyncVideoProcessingService(Runnable::run,
                videoProcessingChain, fulfillmentRecordDao, orderDao);
        FulfillmentRecordDO record = new FulfillmentRecordDO();
        record.setRecordId(1L);
        VideoProcessingContext context = new VideoProcessingContext(record, tempDir.resolve("source.mp4"), 30,
                "watermark");
        doThrow(new ClientException(BaseErrorCode.VIDEO_TRANSCODE_ERROR)).when(videoProcessingChain).process(context);

        service.submit(context);

        verify(fulfillmentRecordDao).updateById(argThat(updatedRecord ->
                VideoProcessingStatus.FAILED.equals(updatedRecord.getProcessingStatus())
                        && BaseErrorCode.VIDEO_TRANSCODE_ERROR.code()
                                .equals(updatedRecord.getProcessingErrorCode())));
    }
}
