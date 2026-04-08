package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘数据视图对象
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人总数
     */
    private Long totalElders;

    /**
     * 服务商总数
     */
    private Long totalProviders;

    /**
     * 服务人员总数
     */
    private Long totalStaff;

    /**
     * 订单总数
     */
    private Long totalOrders;

    /**
     * 今日订单数
     */
    private Long todayOrders;

    /**
     * 今日已完成订单数
     */
    private Long todayCompletedOrders;

    /**
     * 今日待处理订单数
     */
    private Long todayPendingOrders;

    /**
     * 今日已取消订单数
     */
    private Long todayCancelledOrders;

    /**
     * 服务类型分布
     */
    private List<ServiceTypeDistribution> serviceTypeDistribution;

    /**
     * 订单趋势数据（最近7天）
     */
    private List<TrendData> orderTrend;

    /**
     * TOP服务商列表
     */
    private List<TopProvider> topProviders;

    /**
     * 服务类型分布
     */
    @Data
    public static class ServiceTypeDistribution implements Serializable {
        private String serviceTypeName;
        private String serviceTypeCode;
        private Long orderCount;
        private BigDecimal percentage;
    }

    /**
     * 趋势数据
     */
    @Data
    public static class TrendData implements Serializable {
        private String date;
        private Long value;
    }

    /**
     * TOP服务商
     */
    @Data
    public static class TopProvider implements Serializable {
        private String providerId;
        private String providerName;
        private Long orderCount;
        private Double rating;
    }
}
