package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.GeoUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateLocationStep implements FulfillmentNodeStep {

    private static final double MAX_DISTANCE_METERS = 500.0;

    private final OrderAddressSnapshotDao orderAddressSnapshotDao;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.VALIDATE_LOCATION;
    }

    @Override
    public void handle(FulfillmentContext context) {
        if (!context.node().shouldValidateLocation()) {
            return;
        }
        if (context.lat() == null || context.lng() == null) {
            throw new ClientException(BaseErrorCode.FULFILLMENT_LOCATION_REQUIRED_ERROR);
        }
        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(
                context.order().getAddressSnapshotId());
        if (addressSnapshot == null || addressSnapshot.getLatitude() == null
                || addressSnapshot.getLongitude() == null) {
            return;
        }
        double[] gcjCoords = GeoUtils.wgs84ToGcj02(context.lat(), context.lng());
        if (haversineDistance(gcjCoords[0], gcjCoords[1], addressSnapshot.getLatitude(),
                addressSnapshot.getLongitude()) > MAX_DISTANCE_METERS) {
            throw new ClientException(BaseErrorCode.FULFILLMENT_LOCATION_ERROR);
        }
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        return GeoUtils.distanceMeters(lat1, lng1, lat2, lng2);
    }
}
