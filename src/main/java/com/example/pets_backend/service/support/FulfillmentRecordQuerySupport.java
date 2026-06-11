package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.service.fulfillment.FulfillmentNodeType;
import com.example.pets_backend.service.video.VideoProcessingStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FulfillmentRecordQuerySupport {

    private static final int NODE_TYPE_AUTO_RISK_ALERT = 90;

    private FulfillmentRecordQuerySupport() {
    }

    /**
     * 每个打卡节点只保留最新一条有效记录，过滤系统事件与演示种子数据。
     */
    public static List<FulfillmentRecordDO> pickLatestDisplayRecords(List<FulfillmentRecordDO> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        Map<Integer, FulfillmentRecordDO> latestByNode = new LinkedHashMap<>();
        for (FulfillmentRecordDO record : records) {
            if (!isDisplayableChecklistRecord(record)) {
                continue;
            }
            if (isDemoSeedRecord(record) || isPlaceholderRecord(record)) {
                continue;
            }
            latestByNode.put(record.getNodeType(), record);
        }
        return latestByNode.values().stream()
                .sorted(Comparator.comparing(FulfillmentRecordDO::getNodeType))
                .toList();
    }

    public static boolean isDisplayableChecklistRecord(FulfillmentRecordDO record) {
        if (record == null || record.getNodeType() == null) {
            return false;
        }
        if (record.getNodeType() == NODE_TYPE_AUTO_RISK_ALERT) {
            return false;
        }
        if ("SYSTEM_EVENT".equalsIgnoreCase(record.getMediaType())) {
            return false;
        }
        return FulfillmentNodeType.getDescByCode(record.getNodeType()) != null;
    }

    public static boolean isPlaceholderRecord(FulfillmentRecordDO record) {
        if (record == null) {
            return false;
        }
        String imageUrl = record.getImageUrl();
        if (imageUrl != null) {
            String normalized = imageUrl.toLowerCase();
            if (normalized.contains("example.com") || normalized.contains("/seed/default/")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDemoSeedRecord(FulfillmentRecordDO record) {
        if (record == null) {
            return false;
        }
        String objectKey = record.getObjectKey();
        if (objectKey != null) {
            if (objectKey.startsWith("seed/") || objectKey.contains("/demo/")) {
                return true;
            }
        }
        String imageUrl = record.getImageUrl();
        return imageUrl != null && imageUrl.contains("/seed/default/");
    }

    public static boolean blocksRetry(FulfillmentRecordDO record) {
        if (isDemoSeedRecord(record)) {
            return false;
        }
        String status = record.getProcessingStatus();
        if (status == null || status.isBlank()) {
            return true;
        }
        return !VideoProcessingStatus.FAILED.equals(status);
    }

    public static List<FulfillmentRecordDO> excludingDemoSeed(List<FulfillmentRecordDO> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        List<FulfillmentRecordDO> filtered = new ArrayList<>();
        for (FulfillmentRecordDO record : records) {
            if (!isDemoSeedRecord(record)) {
                filtered.add(record);
            }
        }
        return filtered;
    }
}
