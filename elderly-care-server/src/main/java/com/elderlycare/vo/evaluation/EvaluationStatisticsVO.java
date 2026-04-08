package com.elderlycare.vo.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 评价统计VO
 */
@Data
public class EvaluationStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总评价数
     */
    private Long totalCount;

    /**
     * 平均评分
     */
    private BigDecimal averageRating;

    /**
     * 总评价数
     */
    private Long fiveStarCount;

    private Long fourStarCount;

    private Long threeStarCount;

    private Long twoStarCount;

    private Long oneStarCount;

    /**
     * 按服务类型统计
     */
    private List<ServiceTypeStatistics> byServiceType;

    /**
     * 按服务商统计
     */
    private List<ProviderStatistics> byProvider;

    @Data
    public static class ServiceTypeStatistics implements Serializable {
        private String serviceTypeCode;
        private String serviceTypeName;
        private Long count;
        private BigDecimal averageRating;
    }

    @Data
    public static class ProviderStatistics implements Serializable {
        private String providerId;
        private String providerName;
        private Long count;
        private BigDecimal averageRating;
    }
}
