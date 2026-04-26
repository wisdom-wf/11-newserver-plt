package com.elderlycare.entity.staff;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务人员评分实体
 */
@Data
@TableName("t_staff_score")
public class StaffScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String scoreId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 服务商ID
     */
    private String providerId;

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
