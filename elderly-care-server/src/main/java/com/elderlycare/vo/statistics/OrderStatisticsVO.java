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

    /**
     * 订单总数
     */
    private Long totalOrders;

    /**
     * 完成订单数
     */
    private Long completedOrders;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 订单总额
     */
    private BigDecimal totalAmount;

    /**
     * 按服务类型分布
     */
    private List<ServiceTypeDistribution> serviceTypeDistribution;

    /**
     * 订单趋势数据
     */
    private List<TrendData> orderTrend;

    /**
     * 订单来源分布
     */
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
