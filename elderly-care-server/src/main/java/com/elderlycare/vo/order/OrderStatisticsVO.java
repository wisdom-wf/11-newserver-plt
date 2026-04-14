package com.elderlycare.vo.order;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计VO
 */
@Data
public class OrderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 核心数量 ====================
    /** 总订单数 */
    private Integer total;

    /** 今日新增 */
    private Integer today;

    /** 本月新增 */
    private Integer month;

    // ==================== 状态分布 ====================
    /** 待派单数 */
    private Integer pendingDispatch;

    /** 已派单数 */
    private Integer dispatched;

    /** 已接单数 */
    private Integer received;

    /** 服务中数 */
    private Integer inService;

    /** 已完成数 */
    private Integer completed;

    /** 已取消数 */
    private Integer cancelled;

    // ==================== 比率 ====================
    /** 完成率(%) */
    private BigDecimal completionRate;

    /** 取消率(%) */
    private BigDecimal cancelRate;

    // ==================== 金额统计 ====================
    /** 总预估金额 */
    private BigDecimal totalEstimatedPrice;

    /** 总实际金额 */
    private BigDecimal totalActualPrice;

    /** 总补贴金额 */
    private BigDecimal totalSubsidy;

    /** 总自付金额 */
    private BigDecimal totalSelfPay;

    // ==================== 服务人员排名 ====================
    /** 服务人员排名列表 */
    private List<StaffRanking> staffRankings;

    @Data
    public static class StaffRanking implements Serializable {
        private String staffId;
        private String staffName;
        private String providerName;
        private Integer orderCount;
        private Integer completedCount;
    }
}
