package com.elderlycare.common;

/**
 * 预约状态枚举
 */
public enum AppointmentStatus {
    PENDING("待确认"),
    CONFIRMED("已确认"),
    ASSIGNED("已分配"),
    IN_SERVICE("服务中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    INVALID("已作废");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static AppointmentStatus fromCode(String code) {
        if (code == null) return null;
        for (AppointmentStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        return null;
    }
}