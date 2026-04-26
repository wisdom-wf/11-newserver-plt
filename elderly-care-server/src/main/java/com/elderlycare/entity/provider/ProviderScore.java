package com.elderlycare.entity.provider;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务商评分实体
 */
@Data
@TableName("t_provider_score")
public class ProviderScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String scoreId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务商名称
     */
    private String providerName;

    /**
     * 评分周期类型：DAY/WEEK/MONTH/QUARTER/YEAR
     */
    private String periodType;

    /**
     * 周期开始日期
     */
    private LocalDate periodStart;

    /**
     * 周期结束日期
     */
    private LocalDate periodEnd;

    /**
     * 态度评分
     */
    private BigDecimal attitudeScore;

    /**
     * 质量评分
     */
    private BigDecimal qualityScore;

    /**
     * 效率评分
     */
    private BigDecimal efficiencyScore;

    /**
     * 环境评分
     */
    private BigDecimal environmentScore;

    /**
     * 总体评分
     */
    private BigDecimal overallScore;

    /**
     * 评价数量
     */
    private Integer evaluationCount;

    /**
     * 投诉数量
     */
    private Integer complaintCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
