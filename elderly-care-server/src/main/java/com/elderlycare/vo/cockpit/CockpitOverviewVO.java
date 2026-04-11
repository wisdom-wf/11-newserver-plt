package com.elderlycare.vo.cockpit;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 驾驶舱概览数据
 */
@Data
public class CockpitOverviewVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 今日订单 */
    private Long todayOrders;
    /** 本月订单 */
    private Long monthOrders;
    /** 累计订单 */
    private Long totalOrders;
    /** 今日服务人次 */
    private Long todayServices;
    /** 本月服务人次 */
    private Long monthServices;
    /** 累计服务人次 */
    private Long totalServices;
    /** 服务商数量 */
    private Long providerCount;
    /** 服务人员数量 */
    private Long staffCount;
    /** 老人数量 */
    private Long elderCount;
    /** 本月营收 */
    private BigDecimal monthRevenue;
    /** 累计营收 */
    private BigDecimal totalRevenue;
    /** 满意度 */
    private BigDecimal satisfaction;
    /** 合格率 */
    private BigDecimal qualifiedRate;

    /** 服务类型分布 */
    private List<ServiceDistribution> serviceTypeDistribution;
    /** 区域分布 */
    private List<AreaDistribution> areaDistribution;
    /** 服务商排行 */
    private List<ProviderRanking> providerRanking;
    /** 服务人员排行 */
    private List<StaffRanking> staffRanking;

    @Data
    public static class ServiceDistribution implements Serializable {
        private String category;
        private Long count;
        private BigDecimal proportion;
    }

    @Data
    public static class AreaDistribution implements Serializable {
        private String areaId;
        private String areaName;
        private Long orderCount;
        private Long serviceCount;
        private BigDecimal amount;
        private BigDecimal proportion;
    }

    @Data
    public static class ProviderRanking implements Serializable {
        private String providerId;
        private String providerName;
        private Long orderCount;
        private Long serviceCount;
        private Double rating;
        private BigDecimal amount;
    }

    @Data
    public static class StaffRanking implements Serializable {
        private String staffId;
        private String staffName;
        private String providerName;
        private Long orderCount;
        private Long serviceCount;
        private Double rating;
    }
}
