package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计视图对象（匹配前端 Api.Order.Statistics）
 */
@Data
public class OrderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 核心数量
    private Long total;
    private Long today;
    private Long month;

    // 状态分布（匹配前端字段名）
    private Long pendingDispatch;
    private Long dispatched;
    private Long received;
    private Long inService;
    private Long completed;
    private Long cancelled;

    // 比率
    private BigDecimal completionRate;
    private BigDecimal cancelRate;

    // 金额统计
    private BigDecimal totalEstimatedPrice;
    private BigDecimal totalActualPrice;
    private BigDecimal totalSubsidy;
    private BigDecimal totalSelfPay;

    // 额外统计
    private Double avgDuration;
    private Double averageRating;
    private BigDecimal totalAmount;
    private List<ServiceTypeDistribution> serviceTypeDistribution;
    private List<TrendData> orderTrend;
    private List<OrderSourceDistribution> orderSourceDistribution;

    // 服务人员排名
    private List<StaffRanking> staffRankings;

    @Data
    public static class ServiceTypeDistribution implements Serializable {
        private String serviceTypeCode;
        private String serviceTypeName;
        private Long orderCount;
        private Long completedCount;
        private BigDecimal completionRate;
        private BigDecimal totalAmount;
    }

    @Data
    public static class TrendData implements Serializable {
        private String date;
        private Long orderCount;
        private Long completedCount;
        private BigDecimal amount;
    }

    @Data
    public static class OrderSourceDistribution implements Serializable {
        private String orderSource;
        private String sourceName;
        private Long count;
        private BigDecimal percentage;
    }

    @Data
    public static class StaffRanking implements Serializable {
        private String staffId;
        private String staffName;
        private String providerName;
        private Long orderCount;
        private Long completedCount;
    }
}
