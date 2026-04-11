package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计视图对象
 */
@Data
public class OrderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 基础统计（匹配前端Api.Order.Statistics）
    private Long total;
    private Long today;
    private Long month;
    private Long pending;
    private Long assigned;
    private Long inService;
    private Long completed;
    private Long cancelled;
    private BigDecimal completionRate;
    private Double avgDuration;

    // 额外统计
    private Long totalOrders;
    private Long completedOrders;
    private Double averageRating;
    private BigDecimal totalAmount;
    private List<ServiceTypeDistribution> serviceTypeDistribution;
    private List<TrendData> orderTrend;
    private List<OrderSourceDistribution> orderSourceDistribution;

    /**
     * 服务类型分布
     */
    @Data
    public static class ServiceTypeDistribution implements Serializable {
        private String serviceTypeCode;
        private String serviceTypeName;
        private Long orderCount;
        private Long completedCount;
        private BigDecimal completionRate;
        private BigDecimal totalAmount;
    }

    /**
     * 趋势数据
     */
    @Data
    public static class TrendData implements Serializable {
        private String date;
        private Long orderCount;
        private Long completedCount;
        private BigDecimal amount;
    }

    /**
     * 订单来源分布
     */
    @Data
    public static class OrderSourceDistribution implements Serializable {
        private String orderSource;
        private String sourceName;
        private Long count;
        private BigDecimal percentage;
    }
}
