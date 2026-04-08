package com.elderlycare.vo.financial;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结算单VO
 */
@Data
public class SettlementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String settlementId;

    private String settlementNo;

    private String settlementType;

    private String settlementTypeName;

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
