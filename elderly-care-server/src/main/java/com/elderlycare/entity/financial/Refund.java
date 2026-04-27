package com.elderlycare.entity.financial;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体
 */
@Data
@TableName("t_refund")
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String refundId;

    private String refundNo;

    private String orderId;

    private String settlementId;

    // DB无provider_id
    @TableField(exist = false)
    private String providerId;

    private String refundType;

    private BigDecimal refundAmount;

    private String refundReason;

    private String refundStatus;

    // 逻辑别名：Service层统一用auditStatus，DB层是refundStatus
    public String getAuditStatus() { return this.refundStatus; }
    public void setAuditStatus(String status) { this.refundStatus = status; }

    private String applicantId;

    private String approverId;

    // 映射 DB approval_time，Service层用 getAuditTime()/setAuditTime()
    @TableField("approval_time")
    private LocalDateTime auditTime;

    private LocalDateTime refundTime;

    // 联表字段
    @TableField(exist = false)
    private String auditComment;

    @TableField(exist = false)
    private String auditorId;

    @TableField(exist = false)
    private String auditorName;

    @TableField(exist = false)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // DB无update_time列，但Service层需要
    @TableField(exist = false)
    private LocalDateTime updateTime;

}
