package com.elderlycare.vo.servicelog;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务日志统计VO
 */
@Data
public class ServiceLogStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总服务次数 */
    private Integer totalServices;

    /** 总服务时长（分钟） */
    private Integer totalDuration;

    /** 今日服务次数 */
    private Integer todayServices;

    /** 本周服务次数 */
    private Integer weekServices;

    /** 本月服务次数 */
    private Integer monthServices;

    /** 平均服务评分 */
    private BigDecimal avgScore;

    /** 异常服务次数 */
    private Integer anomalyCount;
}
