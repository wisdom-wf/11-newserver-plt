package com.elderlycare.entity.order;

/**
 * 订单状态枚举
 * 数字编码与前端保持一致
 */
public enum OrderStatus {

    CANCELLED("CANCELLED", "已取消", 0),
    CREATED("CREATED", "待派单", 2),
    DISPATCHED("DISPATCHED", "已派单", 3),
    RECEIVED("RECEIVED", "已接单", 4),
    SERVICE_STARTED("SERVICE_STARTED", "服务中", 5),
    SERVICE_COMPLETED("SERVICE_COMPLETED", "已完成", 6),
    EVALUATED("EVALUATED", "已评价", 7),
    SETTLED("SETTLED", "已结算", 8),
    REJECTED("REJECTED", "已拒单", 9);

    private final String code;
    private final String description;
    private final int numericCode;

    OrderStatus(String code, String description, int numericCode) {
        this.code = code;
        this.description = description;
        this.numericCode = numericCode;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getNumericCode() {
        return numericCode;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static OrderStatus fromNumericCode(int numericCode) {
        for (OrderStatus status : values()) {
            if (status.numericCode == numericCode) {
                return status;
            }
        }
        return null;
    }
}
