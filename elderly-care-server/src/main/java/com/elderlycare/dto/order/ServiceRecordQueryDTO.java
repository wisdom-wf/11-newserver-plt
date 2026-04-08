package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 服务记录查询DTO
 */
@Data
public class ServiceRecordQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String staffId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String orderId;
}
