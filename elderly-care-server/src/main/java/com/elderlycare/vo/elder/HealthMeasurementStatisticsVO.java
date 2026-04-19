package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 健康测量统计VO
 */
@Data
public class HealthMeasurementStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 测量类型
     */
    private String measurementType;

    /**
     * 测量类型名称
     */
    private String measurementTypeName;

    /**
     * 记录数量
     */
    private Integer count;

    /**
     * 最新测量值
     */
    private String latestValue;

    /**
     * 最新测量时间
     */
    private String latestTime;

    /**
     * 平均值（数值类型或血压格式字符串如 "120/80"）
     */
    private Object averageValue;

    /**
     * 最大值（数值类型或血压格式字符串如 "140/90"）
     */
    private Object maxValue;

    /**
     * 最小值（数值类型或血压格式字符串如 "100/60"）
     */
    private Object minValue;

    /**
     * 趋势数据（最近N次测量）
     */
    private List<Map<String, Object>> trendData;

    /**
     * 预警状态：NORMAL-正常, WARNING-预警, ALERT-告警
     */
    private String alertStatus;

    /**
     * 预警信息
     */
    private String alertMessage;
}
