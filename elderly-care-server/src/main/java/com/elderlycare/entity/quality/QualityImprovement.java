package com.elderlycare.entity.quality;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 质量改进实体
 */
@Data
@TableName("t_quality_improvement")
public class QualityImprovement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String improvementId;

    /**
     * 改进标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String problemDescription;

    /**
     * 根本原因
     */
    private String rootCause;

    /**
     * 改进计划
     */
    private String improvementPlan;

    /**
     * 改进措施
     */
    private String measures;

    /**
     * 责任人
     */
    private String responsiblePerson;

    /**
     * 责任人电话
     */
    private String responsiblePhone;

    /**
     * 关联服务商ID
     */
    private String providerId;

    /**
     * 关联服务人员ID
     */
    private String staffId;

    /**
     * 关联预警ID
     */
    private String alertId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 目标完成日期
     */
    private LocalDate targetDate;

    /**
     * 实际完成日期
     */
    private LocalDate completionDate;

    /**
     * 状态：PENDING-待处理/IN_PROGRESS-进行中/COMPLETED-已完成/EVALUATED-已评估
     */
    private String status;

    /**
     * 效果评估
     */
    private String effectEvaluation;

    /**
     * 评估人ID
     */
    private String evaluatorId;

    /**
     * 评估人姓名
     */
    private String evaluatorName;

    /**
     * 评估时间
     */
    private LocalDateTime evaluateTime;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
