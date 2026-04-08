package com.elderlycare.vo.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 财务报表VO
 */
@Data
public class FinancialReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate reportDate;

    private String providerId;

    private String providerName;

    private Integer totalOrderCount;

    private Integer completedOrderCount;

    private Integer cancelledOrderCount;

    private BigDecimal totalServiceAmount;

    private BigDecimal totalGovernmentSubsidy;

    private BigDecimal totalSelfPay;

    private BigDecimal totalSettlementAmount;

    private BigDecimal totalRefundAmount;

    private BigDecimal netAmount;
}
