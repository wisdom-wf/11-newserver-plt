package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 订单查询DTO
 */
@Data
public class OrderQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String elderId;

    private String elderName;

    private String serviceTypeCode;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private String providerId;

    private String staffId;

    private String orderNo;
}
