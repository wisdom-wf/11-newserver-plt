package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款申请DTO
 */
@Data
public class RefundCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderId;

    private String orderNo;

    private String elderId;

    private String elderName;

    private String providerId;

    private String providerName;

    private BigDecimal refundAmount;

    private String refundReason;

    private String refundType;

    private String remark;
}
