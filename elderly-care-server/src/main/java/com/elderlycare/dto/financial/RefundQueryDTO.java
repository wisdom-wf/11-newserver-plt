package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;

/**
 * 退款查询DTO
 */
@Data
public class RefundQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer page = 1;

    private Integer pageSize = 10;

    private String refundNo;

    private String orderId;

    private String elderId;

    private String providerId;

    private String auditStatus;

    private String refundType;
}
