package com.elderlycare.vo.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录VO
 */
@Data
public class RefundVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private String auditStatus;

    private String auditStatusName;

    private String auditComment;

    private String auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
