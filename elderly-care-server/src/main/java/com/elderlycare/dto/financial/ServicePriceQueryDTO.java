package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;

/**
 * 服务定价查询DTO
 */
@Data
public class ServicePriceQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer page = 1;

    private Integer pageSize = 10;

    private String serviceTypeCode;

    private String providerId;

    private String status;

    private String priceType;
}
