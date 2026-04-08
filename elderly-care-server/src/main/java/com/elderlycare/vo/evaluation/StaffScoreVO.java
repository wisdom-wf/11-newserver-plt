package com.elderlycare.vo.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务人员评分VO
 */
@Data
public class StaffScoreVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String staffId;

    private String staffName;

    private String providerId;

    private String providerName;

    /**
     * 平均评分
     */
    private BigDecimal averageRating;

    /**
     * 评价数量
     */
    private Integer evaluationCount;

    /**
     * 五星数量
     */
    private Integer fiveStarCount;

    /**
     * 四星数量
     */
    private Integer fourStarCount;

    /**
     * 三星数量
     */
    private Integer threeStarCount;

    /**
     * 二星数量
     */
    private Integer twoStarCount;

    /**
     * 一星数量
     */
    private Integer oneStarCount;

    /**
     * 平均态度评分
     */
    private BigDecimal averageAttitudeScore;

    /**
     * 平均质量评分
     */
    private BigDecimal averageQualityScore;

    /**
     * 平均效率评分
     */
    private BigDecimal averageEfficiencyScore;
}
