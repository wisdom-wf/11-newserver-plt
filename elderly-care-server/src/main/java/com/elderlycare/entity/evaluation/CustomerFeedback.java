package com.elderlycare.entity.evaluation;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户反馈实体
 */
@Data
@TableName("t_customer_feedback")
public class CustomerFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String feedbackId;

    /**
     * 反馈类型(COMPLAINT-投诉, SUGGESTION-建议, PRAISE-表扬, OTHER-其他)
     */
    private String feedbackType;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 老人电话
     */
    private String elderPhone;

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
     * 订单ID(可选)
     */
    private String orderId;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 反馈图片(多个逗号分隔)
     */
    private String photos;

    /**
     * 处理状态(PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, REJECTED-已驳回)
     */
    private String handleStatus;

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
    private String handlingResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 反馈状态(0-隐藏,1-显示)
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
