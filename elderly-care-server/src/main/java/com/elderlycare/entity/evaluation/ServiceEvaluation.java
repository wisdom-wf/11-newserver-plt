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
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务商名称
     */
    private String providerName;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 服务类型编码
     */
    private String serviceTypeCode;

    /**
     * 服务类型名称
     */
    private String serviceTypeName;

    /**
     * 总体评分(1-5)
     */
    private Integer rating;

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
     * 评价内容
     */
    private String content;

    /**
     * 评价标签(多个逗号分隔)
     */
    private String tags;

    /**
     * 是否匿名(0-否,1-是)
     */
    private Integer anonymous;

    /**
     * 评价状态(0-隐藏,1-显示)
     */
    private Integer status;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
