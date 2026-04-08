package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新订单DTO
 */
@Data
public class UpdateOrderDTO implements Serializable {

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

    private String subsidyType;

    private BigDecimal estimatedPrice;

    private BigDecimal subsidyAmount;

    private BigDecimal selfPayAmount;
}
