package com.example.pets_backend.service.support;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeoUtils() {
    }

    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * 设备 GPS 为 WGS-84，地图/订单地址为 GCJ-02，距离校验前需统一坐标系。
     */
    public static double[] wgs84ToGcj02(double wgsLat, double wgsLng) {
        if (outOfChina(wgsLat, wgsLng)) {
            return new double[] {wgsLat, wgsLng};
        }
        double dLat = transformLat(wgsLng - 105.0, wgsLat - 35.0);
        double dLng = transformLng(wgsLng - 105.0, wgsLat - 35.0);
        double radLat = wgsLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        double sqrtMagic = Math.sqrt(1 - 0.00669342162296594323 * magic * magic);
        double finalDLat = (dLat * 180.0)
                / ((6378245.0 * (1 - 0.00669342162296594323))
                        / (sqrtMagic * sqrtMagic * sqrtMagic)
                        * Math.PI);
        double finalDLng = (dLng * 180.0) / (6378245.0 / sqrtMagic * Math.cos(radLat) * Math.PI);
        return new double[] {wgsLat + finalDLat, wgsLng + finalDLng};
    }

    private static boolean outOfChina(double lat, double lng) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    public static Double distanceKm(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return null;
        }
        return roundToOneDecimal(distanceMeters(lat1, lng1, lat2, lng2) / 1000.0);
    }

    public static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
