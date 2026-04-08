package com.elderlycare.vo.statistics;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 质量统计视图对象
 */
@Data
public class QualityStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 好评率
     */
    private BigDecimal positiveRate;

    /**
     * 投诉率
     */
    private BigDecimal complaintRate;

    /**
     * 评价总数
     */
    private Long totalEvaluations;

    /**
     * 好评数
     */
    private Long positiveCount;

    /**
     * 中评数
     */
    private Long neutralCount;

    /**
     * 差评数
     */
    private Long negativeCount;

    /**
     * 投诉数
     */
    private Long complaintCount;

    /**
     * 评分趋势（最近7天）
     */
    private List<RatingTrend> ratingTrend;

    /**
     * 常见投诉类型
     */
    private List<ComplaintType> complaintTypes;

    /**
     * 评分趋势
     */
    @Data
    public static class RatingTrend implements Serializable {
        private String date;
        private Double averageRating;
        private Long evaluationCount;
    }

    /**
     * 投诉类型
     */
    @Data
    public static class ComplaintType implements Serializable {
        private String complaintType;
        private String typeName;
        private Long count;
        private BigDecimal percentage;
    }
}
