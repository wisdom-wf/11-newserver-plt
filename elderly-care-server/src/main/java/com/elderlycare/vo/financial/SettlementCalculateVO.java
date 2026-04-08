package com.elderlycare.vo.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 结算计算结果VO
 */
@Data
public class SettlementCalculateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String providerId;

    private String providerName;

    private String staffId;

    private String staffName;

    private LocalDate settlementPeriodStart;

    private LocalDate settlementPeriodEnd;

    private Integer totalOrderCount;

    private BigDecimal totalServiceAmount;

    private BigDecimal totalSubsidyAmount;

    private BigDecimal totalSelfPayAmount;

    private BigDecimal calculatedSettlementAmount;
}
