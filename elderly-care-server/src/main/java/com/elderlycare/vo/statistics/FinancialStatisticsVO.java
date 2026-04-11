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
     * 待结算数
     */
    private Long pending;

    /**
     * 已结算数
     */
    private Long completed;

    /**
     * 本月结算金额
     */
    private BigDecimal monthAmount;

    /**
     * 累计结算金额
     */
    private BigDecimal totalAmount;

    /**
     * 服务费统计
     */
    private BigDecimal serviceFeeTotal;

    /**
     * 补贴统计
     */
    private BigDecimal subsidyTotal;

    /**
     * 自付统计
     */
    private BigDecimal selfPayTotal;

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
