package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 结算单查询DTO
 */
@Data
public class SettlementQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer page = 1;

    private Integer pageSize = 10;

    private String settlementNo;

    private String providerId;

    private String staffId;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private String settlementType;
}
