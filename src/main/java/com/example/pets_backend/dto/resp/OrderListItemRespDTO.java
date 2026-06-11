package com.example.pets_backend.dto.resp;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单列表项响应参数。
 *
 * @param orderId 订单ID
 * @param ownerId 宠主ID
 * @param providerId 服务方ID
 * @param orderStatus 订单状态
 * @param orderStatusDesc 订单状态描述
 * @param totalAmount 订单金额
 * @param serviceDate 服务日期
 */
public record OrderListItemRespDTO(
        Long orderId,
        Long ownerId,
        Long providerId,
        Integer orderStatus,
        String orderStatusDesc,
        BigDecimal totalAmount,
        LocalDate serviceDate) {
}
