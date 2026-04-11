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
    private Integer total;

    /** 今日服务次数 */
    private Integer today;

    /** 本月服务次数 */
    private Integer month;

    /** 平均服务时长（分钟） */
    private BigDecimal avgDuration;

    /** 异常服务次数 */
    private Integer anomalyCount;
}
