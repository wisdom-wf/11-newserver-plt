package com.elderlycare.dto.ess;

import lombok.Data;

@Data
public class ContractQueryDTO {

    private String contractNo;

    private String status;

    private String startDate;

    private String endDate;

    private Integer page = 1;

    private Integer pageSize = 10;
}