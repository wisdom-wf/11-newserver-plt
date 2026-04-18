package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
public class OrderDetailVO implements Serializable {

    private String orderId;

    private String orderNo;

    private String elderId;

    private String elderName;

    private String elderPhone;

    private String serviceTypeCode;

    private String serviceTypeName;

    private LocalDate serviceDate;

    private String serviceTime;

    private Integer serviceDuration;

    private String serviceAddress;

    private String specialRequirements;

    private String orderType;

    private String orderSource;

    private String subsidyType;

    private BigDecimal estimatedPrice;

    private BigDecimal actualPrice;

    private BigDecimal subsidyAmount;

    private BigDecimal selfPayAmount;

    private String status;

    private Integer statusCode;

    private String statusName;

    private String providerId;

    private String providerName;

    private String staffId;

    private String staffName;

    private String staffPhone;

    private String cancelReason;

    private LocalDateTime dispatchTime;

    private LocalDateTime receiveTime;

    private LocalDateTime startTime;

    private LocalDateTime completeTime;

    private LocalDateTime createTime;

    private List<OrderTimelineVO> timeline;
}
