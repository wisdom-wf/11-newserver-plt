package com.elderlycare.vo.servicelog;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务日志统计VO
 */
@Data
public class ServiceLogStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 核心数量统计 ====================
    /** 总服务日志数 */
    private Integer total;

    /** 今日新增日志数 */
    private Integer today;

    /** 本月新增日志数 */
    private Integer month;

    // ==================== 审核状态统计 ====================
    /** 待审核数 */
    private Integer pendingCount;

    /** 已通过数 */
    private Integer approvedCount;

    /** 已驳回数 */
    private Integer rejectedCount;

    /** 审核通过率(%) */
    private BigDecimal approvalRate;

    /** 待审核率(%) */
    private BigDecimal pendingRate;

    // ==================== 服务质量统计 ====================
    /** 平均服务时长(分钟) */
    private BigDecimal avgDuration;

    /** 平均服务评分 */
    private BigDecimal avgScore;

    /** 异常服务次数 */
    private Integer anomalyCount;

    /** 异常率(%) */
    private BigDecimal anomalyRate;

    // ==================== 审核效率统计 ====================
    /** 平均审核耗时(小时) */
    private BigDecimal avgReviewTime;

    // ==================== 服务人员排名 ====================
    /** 服务人员排名列表 */
    private List<StaffRanking> staffRankings;

    @Data
    public static class StaffRanking implements Serializable {
        private String staffId;
        private String staffName;
        private String providerName;
        private Integer logCount;
        private Integer approvedCount;
        private Integer rejectedCount;
        private BigDecimal approvalRate;
    }
}
