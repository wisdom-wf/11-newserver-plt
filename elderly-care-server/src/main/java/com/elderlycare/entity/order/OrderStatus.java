package com.elderlycare.entity.order;

/**
 * 订单状态枚举
 */
public enum OrderStatus {

    CREATED("CREATED", "待派单"),
    DISPATCHED("DISPATCHED", "已派单"),
    RECEIVED("RECEIVED", "已接单"),
    SERVICE_STARTED("SERVICE_STARTED", "服务中"),
    SERVICE_COMPLETED("SERVICE_COMPLETED", "已完成"),
    EVALUATED("EVALUATED", "已评价"),
    SETTLED("SETTLED", "已结算"),
    CANCELLED("CANCELLED", "已取消"),
    REJECTED("REJECTED", "已拒单");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
