package com.elderlycare.common;

import java.util.Map;
import java.util.HashMap;

/**
 * 服务类型枚举
 */
public enum ServiceTypeCode {
    DOOR_TO_DOOR("上门服务"),
    DAY_CARE("日间照料"),
    MEAL_DELIVERY("助餐服务"),
    CLEANING("助洁服务"),
    LAUNDRY("助浴服务"),
    LAUNDRY_ASSIST("助洗服务"),
    HEALTH_MONITORING("健康管理"),
    REHABILITATION("康复护理"),
    MENTAL_SUPPORT("精神慰藉"),
    INFORMATION("信息咨询"),
    EMERGENCY("紧急救援");

    private final String chineseName;
    private static final Map<String, ServiceTypeCode> BY_CHINESE = new HashMap<>();

    static {
        for (ServiceTypeCode code : values()) {
            BY_CHINESE.put(code.chineseName, code);
        }
    }

    ServiceTypeCode(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public static ServiceTypeCode fromChinese(String chinese) {
        if (chinese == null) return null;
        return BY_CHINESE.get(chinese);
    }

    public static ServiceTypeCode fromCode(String code) {
        if (code == null) return null;
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}