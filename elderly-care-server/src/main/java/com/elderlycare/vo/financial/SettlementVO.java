package com.elderlycare.vo.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SettlementVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String settlementId;
    private String settlementNo;
    private String providerId;
    private String providerName;
    private String staffId;
    private String staffName;
    private String orderId;
    private String orderNo;
    private String elderId;
    private String elderName;
    private LocalDate serviceDate;
    private LocalDate settlementPeriodStart;
    private LocalDate settlementPeriodEnd;
    private Integer totalOrderCount;
    private BigDecimal totalServiceAmount;
    private BigDecimal totalSubsidyAmount;
    private BigDecimal totalSelfPayAmount;
    private BigDecimal settlementAmount;
    private String status;
    private String statusName;
    private LocalDateTime confirmTime;
    private String confirmUserId;
    private String confirmUserName;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
