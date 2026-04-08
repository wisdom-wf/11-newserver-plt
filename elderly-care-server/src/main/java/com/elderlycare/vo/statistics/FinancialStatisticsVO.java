package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 财务统计视图对象
 */
@Data
public class FinancialStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 政府补贴总额
     */
    private BigDecimal totalSubsidyAmount;

    /**
     * 自付金额总额
     */
    private BigDecimal totalSelfPayAmount;

    /**
     * 平台费总额
     */
    private BigDecimal totalPlatformFee;

    /**
     * 本月金额
     */
    private BigDecimal monthlyAmount;

    /**
     * 本月政府补贴
     */
    private BigDecimal monthlySubsidyAmount;

    /**
     * 本月自付金额
     */
    private BigDecimal monthlySelfPayAmount;

    /**
     * 月度趋势
     */
    private List<MonthlyTrend> monthlyTrend;

    /**
     * 按服务类型分布
     */
    private List<ServiceTypeDistribution> serviceTypeDistribution;

    /**
     * 月度趋势
     */
    @Data
    public static class MonthlyTrend implements Serializable {
        private String month;
        private BigDecimal totalAmount;
        private BigDecimal subsidyAmount;
        private BigDecimal selfPayAmount;
        private Long orderCount;
    }

    /**
     * 服务类型分布
     */
    @Data
    public static class ServiceTypeDistribution implements Serializable {
        private String serviceTypeCode;
        private String serviceTypeName;
        private BigDecimal totalAmount;
        private BigDecimal subsidyAmount;
        private BigDecimal selfPayAmount;
        private Long orderCount;
        private BigDecimal percentage;
    }
}
