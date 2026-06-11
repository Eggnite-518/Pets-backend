package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.resp.FulfillmentUploadRespDTO;
import com.example.pets_backend.service.video.VideoProcessingContext;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public class FulfillmentContext {

    private final Long orderId;
    private final FulfillmentNodeType node;
    private final MultipartFile file;
    private final Double lat;
    private final Double lng;

    private OrderDO order;
    private FulfillmentRecordDO record;
    private Path sourceFile;
    private VideoProcessingContext videoProcessingContext;

    public FulfillmentContext(Long orderId, FulfillmentNodeType node, MultipartFile file, Double lat, Double lng) {
        this.orderId = orderId;
        this.node = node;
        this.file = file;
        this.lat = lat;
        this.lng = lng;
    }

    public Long orderId() {
        return orderId;
    }

    public FulfillmentNodeType node() {
        return node;
    }

    public MultipartFile file() {
        return file;
    }

    public Double lat() {
        return lat;
    }

    public Double lng() {
        return lng;
    }

    public OrderDO order() {
        return order;
    }

    public void order(OrderDO order) {
        this.order = order;
    }

    public FulfillmentRecordDO record() {
        return record;
    }

    public void record(FulfillmentRecordDO record) {
        this.record = record;
    }

    public Path sourceFile() {
        return sourceFile;
    }

    public void sourceFile(Path sourceFile) {
        this.sourceFile = sourceFile;
    }

    public VideoProcessingContext videoProcessingContext() {
        return videoProcessingContext;
    }

    public void videoProcessingContext(VideoProcessingContext videoProcessingContext) {
        this.videoProcessingContext = videoProcessingContext;
    }

    public FulfillmentUploadRespDTO toResponse() {
        return new FulfillmentUploadRespDTO(
                record.getRecordId(),
                record.getOrderId(),
                record.getNodeType(),
                FulfillmentNodeType.getDescByCode(record.getNodeType()),
                record.getMediaType(),
                record.getObjectKey(),
                record.getImageUrl(),
                record.getFileSize(),
                record.getContentType(),
                record.getFrameRate(),
                record.getProcessingStatus());
    }
}
