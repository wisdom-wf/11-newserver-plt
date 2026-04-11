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

    private String orderNo;

    private String elderId;

    private String elderName;

    private String providerId;

    private String providerName;

    private BigDecimal refundAmount;

    private String refundReason;

    private String refundType;

    @TableField("refund_status")
    private String auditStatus;

    private String auditComment;

    private String auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
