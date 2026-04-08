package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 服务定价创建DTO
 */
@Data
public class ServicePriceCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceTypeCode;

    private String serviceTypeName;

    private String providerId;

    private String providerName;

    private BigDecimal governmentPrice;

    private BigDecimal selfPayPrice;

    private BigDecimal totalPrice;

    private String priceType;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String remark;
}
