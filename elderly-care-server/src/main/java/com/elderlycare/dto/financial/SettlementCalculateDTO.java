package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 结算计算DTO
 */
@Data
public class SettlementCalculateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String providerId;

    private String staffId;

    private LocalDate settlementPeriodStart;

    private LocalDate settlementPeriodEnd;
}
