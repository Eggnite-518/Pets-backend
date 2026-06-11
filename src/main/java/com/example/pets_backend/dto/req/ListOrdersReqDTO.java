package com.example.pets_backend.dto.req;

/**
 * 订单列表查询请求参数。
 *
 * @param orderStatus 订单状态筛选
 * @param petType 宠物类型筛选
 * @param sortType 排序类型
 */
public record ListOrdersReqDTO(
        Integer orderStatus,
        Integer petType,
        Integer sortType) {
}
