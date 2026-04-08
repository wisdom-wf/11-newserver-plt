package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO implements Serializable {

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

    private BigDecimal subsidyAmount;

    private BigDecimal selfPayAmount;
}
