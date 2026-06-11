package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    void distanceKmReturnsRoundedValue() {
        Double distance = GeoUtils.distanceKm(31.2304, 121.4737, 31.2300, 121.4700);

        assertEquals(0.4, distance);
    }

    @Test
    void wgs84ToGcj02ShiftsBeijingCoordinate() {
        double[] gcj = GeoUtils.wgs84ToGcj02(39.948856, 116.335814);

        assertEquals(39.9502, gcj[0], 0.002);
        assertEquals(116.336, gcj[1], 0.002);
    }
}
