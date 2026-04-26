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

    /** 总服务次数 */
    private Integer total;

    /** 今日服务次数 */
    private Integer today;

    /** 本月服务次数 */
    private Integer month;

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

    /** 平均服务时长（分钟） */
    private BigDecimal avgDuration;

    /** 平均服务评分 */
    private BigDecimal avgScore;

    /** 异常服务次数 */
    private Integer anomalyCount;

    /** 异常率(%) */
    private BigDecimal anomalyRate;

    /** 平均审核耗时(小时) */
    private BigDecimal avgReviewTime;

    /** 服务人员排名列表 */
    private List<StaffRankingVO> staffRankings;

    @Data
    public static class StaffRankingVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String staffId;
        private String staffName;
        private String providerName;
        private Integer logCount;
        private Integer approvedCount;
        private Integer rejectedCount;
        private BigDecimal approvalRate;
    }
}
