package com.elderlycare.entity.evaluation;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务评价实体
 */
@Data
@TableName("t_service_evaluation")
public class ServiceEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String evaluationId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名(联表)
     */
    private String elderName;

    /**
     * 服务类型编码(联表)
     */
    private String serviceTypeCode;

    /**
     * 服务类型名称(联表)
     */
    private String serviceTypeName;

    /**
     * 服务人员姓名(联表)
     */
    private String staffName;

    /**
     * 服务商名称(联表)
     */
    private String providerName;

    /**
     * 态度评分(1-5)
     */
    private Integer attitudeScore;

    /**
     * 质量评分(1-5)
     */
    private Integer qualityScore;

    /**
     * 效率评分(1-5)
     */
    private Integer efficiencyScore;

    /**
     * 总体评分(1-5)
     */
    private Integer overallScore;

    /**
     * 平均评分
     */
    private BigDecimal averageScore;

    /**
     * 评价内容
     */
    @TableField("evaluation_content")
    private String content;

    /**
     * 评价标签(多个逗号分隔)
     */
    @TableField("evaluation_tags")
    private String tags;

    /**
     * 是否匿名(0-否,1-是)
     */
    @TableField("is_anonymous")
    private Integer anonymous;

    /**
     * 评价时间
     */
    @TableField("evaluation_time")
    private LocalDateTime evaluationTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
