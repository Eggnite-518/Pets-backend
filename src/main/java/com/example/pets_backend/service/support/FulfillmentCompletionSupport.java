package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.service.fulfillment.ServiceFulfillmentFlow;
import com.example.pets_backend.service.video.VideoProcessingStatus;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FulfillmentCompletionSupport {

    private FulfillmentCompletionSupport() {
    }

    /**
     * 判断订单 checklist 中的每个节点是否都已由宠托师上传（失败节点不计入）。
     */
    public static boolean areAllChecklistNodesUploaded(OrderDO order, FulfillmentRecordDao fulfillmentRecordDao) {
        if (order == null || order.getOrderId() == null || fulfillmentRecordDao == null) {
            return false;
        }
        List<Integer> checklist = ServiceFulfillmentFlow
                .fromServiceType(order.getServiceType())
                .nodeCodes();
        if (checklist.isEmpty()) {
            return false;
        }
        Map<Integer, FulfillmentRecordDO> latestByNode = FulfillmentRecordQuerySupport
                .pickLatestDisplayRecords(fulfillmentRecordDao.selectByOrderId(order.getOrderId()))
                .stream()
                .collect(Collectors.toMap(
                        FulfillmentRecordDO::getNodeType,
                        Function.identity(),
                        (left, right) -> left));
        for (Integer nodeType : checklist) {
            if (!isUploadedNodeRecord(latestByNode.get(nodeType))) {
                return false;
            }
        }
        return true;
    }

    static boolean isUploadedNodeRecord(FulfillmentRecordDO record) {
        if (record == null) {
            return false;
        }
        String status = record.getProcessingStatus();
        if (VideoProcessingStatus.FAILED.equals(status)) {
            return false;
        }
        return true;
    }
}
