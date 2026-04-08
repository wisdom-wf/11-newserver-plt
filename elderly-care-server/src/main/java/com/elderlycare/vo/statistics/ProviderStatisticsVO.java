package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务商统计视图对象
 */
@Data
public class ProviderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务商总数
     */
    private Long totalProviders;

    /**
     * 待审核服务商数
     */
    private Long pendingProviders;

    /**
     * 已审核通过服务商数
     */
    private Long approvedProviders;

    /**
     * 按类型分布
     */
    private List<TypeDistribution> typeDistribution;

    /**
     * TOP服务商排名
     */
    private List<ProviderRanking> providerRankings;

    /**
     * 类型分布
     */
    @Data
    public static class TypeDistribution implements Serializable {
        private String providerType;
        private String typeName;
        private Long count;
        private BigDecimal percentage;
    }

    /**
     * 服务商排名
     */
    @Data
    public static class ProviderRanking implements Serializable {
        private String providerId;
        private String providerName;
        private String providerType;
        private Long orderCount;
        private Long completedOrderCount;
        private Double completionRate;
        private Double averageRating;
        private Double rating;
    }
}
