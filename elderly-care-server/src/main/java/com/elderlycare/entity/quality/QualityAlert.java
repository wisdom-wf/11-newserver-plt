package com.elderlycare.entity.quality;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 质量预警实体
 */
@Data
@TableName("t_quality_alert")
public class QualityAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String alertId;

    /**
     * 预警类型：LOW_SCORE-评分过低/DECLINE-评分下降/COMPLAINT_BURST-投诉增多/BAD_REVIEW_BURST-差评集中
     */
    private String alertType;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 关联评价ID
     */
    private String evaluationId;

    /**
     * 严重程度：LOW/MEDIUM/HIGH/CRITICAL
     */
    private String severity;

    /**
     * 预警内容
     */
    private String alertContent;

    /**
     * 预警状态：PENDING-待处理/HANDLING-处理中/HANDLED-已处理/IGNORED-已忽略
     */
    private String alertStatus;

    /**
     * 处理人ID
     */
    private String handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
