package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 老人统计视图对象
 */
@Data
public class ElderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 基础统计（匹配前端Api.Elder.Statistics）
    private Long total;
    private Long registered;
    private Long suspended;
    private Map<String, Long> careTypeStats;
    private Map<String, Long> careLevelStats;
    private Map<String, Long> subsidyTypeStats;

    // 额外统计
    private Long totalElders;
    private Long monthlyNewElders;
    private Long activeElders;
    private List<AgeDistribution> ageDistribution;
    private List<CareLevelDistribution> careLevelDistribution;
    private List<ServiceDemandDistribution> serviceDemandDistribution;

    /**
     * 年龄段分布
     */
    @Data
    public static class AgeDistribution implements Serializable {
        private String ageRange;
        private String label;
        private Long count;
        private BigDecimal percentage;
    }

    /**
     * 护理级别分布
     */
    @Data
    public static class CareLevelDistribution implements Serializable {
        private Integer careLevel;
        private String levelName;
        private Long count;
        private BigDecimal percentage;
    }

    /**
     * 服务需求分布
     */
    @Data
    public static class ServiceDemandDistribution implements Serializable {
        private String serviceType;
        private String serviceTypeName;
        private Long count;
        private BigDecimal percentage;
    }
}
