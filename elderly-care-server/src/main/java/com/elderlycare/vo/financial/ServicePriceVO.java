package com.elderlycare.vo.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务定价VO
 */
@Data
public class ServicePriceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String priceId;

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

    private String status;

    private String statusName;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
