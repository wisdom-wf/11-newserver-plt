package com.elderlycare.vo.quality;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 质检统计VO
 */
@Data
public class QualityCheckStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总数 */
    private Integer total;

    /** 合格数 */
    private Integer qualifiedCount;

    /** 不合格数 */
    private Integer unqualifiedCount;

    /** 需整改数 */
    private Integer needRectifyCount;

    /** 合格率 */
    private BigDecimal qualifiedRate;

    /** 平均评分 */
    private BigDecimal avgScore;
}
