package com.elderlycare.common;

import java.math.BigDecimal;

/**
 * GPS位置工具类
 */
public class GpsUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 计算两点之间的距离（米）
     * @param lat1 第一个点纬度
     * @param lon1 第一个点经度
     * @param lat2 第二个点纬度
     * @param lon2 第二个点经度
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c * 1000; // 转换为米
    }

    /**
     * 检查位置是否在指定范围内（米）
     * @param currentLat 当前纬度
     * @param currentLon 当前经度
     * @param targetLat 目标纬度
     * @param targetLon 目标经度
     * @param rangeMeters 范围（米）
     * @return 是否在范围内
     */
    public static boolean isWithinRange(double currentLat, double currentLon,
                                        double targetLat, double targetLon,
                                        double rangeMeters) {
        double distance = calculateDistance(currentLat, currentLon, targetLat, targetLon);
        return distance <= rangeMeters;
    }

    /**
     * 解析位置字符串（格式：latitude,longitude）
     * @param location 位置字符串
     * @return double数组 [latitude, longitude]
     */
    public static double[] parseLocation(String location) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("位置信息不能为空");
        }
        String[] parts = location.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("位置格式错误，应为：latitude,longitude");
        }
        return new double[]{Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim())};
    }

    /**
     * 验证签到位置是否在老人住址500米范围内
     * @param signInLocation 签到位置（格式：latitude,longitude）
     * @param elderAddress 老人住址（用于反向地理编码查找坐标，实际项目中应存储坐标）
     * @return 是否在范围内
     */
    public static boolean validateSignInRange(String signInLocation, String elderLatitude, String elderLongitude) {
        if (signInLocation == null || elderLatitude == null || elderLongitude == null) {
            return false;
        }
        try {
            double[] signInCoords = parseLocation(signInLocation);
            double targetLat = Double.parseDouble(elderLatitude);
            double targetLon = Double.parseDouble(elderLongitude);
            return isWithinRange(signInCoords[0], signInCoords[1], targetLat, targetLon, 500);
        } catch (Exception e) {
            return false;
        }
    }
}
