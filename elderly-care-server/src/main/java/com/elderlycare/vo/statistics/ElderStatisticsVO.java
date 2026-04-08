package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 老人统计视图对象
 */
@Data
public class ElderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人总数
     */
    private Long totalElders;

    /**
     * 本月新增老人数
     */
    private Long monthlyNewElders;

    /**
     * 活跃老人数（近30天有订单的老人）
     */
    private Long activeElders;

    /**
     * 按年龄段分布
     */
    private List<AgeDistribution> ageDistribution;

    /**
     * 按护理级别分布
     */
    private List<CareLevelDistribution> careLevelDistribution;

    /**
     * 按服务需求分布
     */
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
