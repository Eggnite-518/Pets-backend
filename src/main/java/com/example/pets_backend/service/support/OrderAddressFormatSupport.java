package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;

/**
 * 订单地址快照统一格式化，确保宠主端与宠托师端展示一致。
 */
public final class OrderAddressFormatSupport {

    private OrderAddressFormatSupport() {
    }

    /** 完整地址：省 + 市 + 区 + 详细地址（与下单快照一致） */
    public static String formatFullAddress(OrderAddressSnapshotDO snapshot) {
        if (snapshot == null) {
            return "";
        }
        return String.join("",
                nullToEmpty(snapshot.getProvince()),
                nullToEmpty(snapshot.getCity()),
                nullToEmpty(snapshot.getDistrict()),
                nullToEmpty(snapshot.getDetailAddress()));
    }

    /** 接单大厅等列表场景：市 + 区，便于区分城市 */
    public static String formatCityDistrict(OrderAddressSnapshotDO snapshot) {
        if (snapshot == null) {
            return "";
        }
        String city = nullToEmpty(snapshot.getCity());
        String district = nullToEmpty(snapshot.getDistrict());
        if (city.isEmpty()) {
            return district;
        }
        if (district.isEmpty()) {
            return city;
        }
        if (district.startsWith(city)) {
            return district;
        }
        return city + district;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
