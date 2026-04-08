package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 批量结算DTO
 */
@Data
public class BatchSettlementDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String settlementType;

    private String providerId;

    private String staffId;

    private LocalDate settlementPeriodStart;

    private LocalDate settlementPeriodEnd;
}
