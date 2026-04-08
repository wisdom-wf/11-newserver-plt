package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 财务报表查询DTO
 */
@Data
public class FinancialReportQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate startDate;

    private LocalDate endDate;

    private String providerId;

    private String staffId;

    private String reportType;
}
