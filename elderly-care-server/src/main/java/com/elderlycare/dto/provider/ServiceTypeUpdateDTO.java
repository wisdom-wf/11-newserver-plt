package com.elderlycare.dto.provider;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务类型更新DTO
 */
@Data
public class ServiceTypeUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型名称
     */
    private String serviceTypeName;

    /**
     * 政府补贴价格
     */
    private BigDecimal subsidyPrice;

    /**
     * 市场价格
     */
    private BigDecimal servicePrice;

    /**
     * 服务区域
     */
    private String serviceArea;

    /**
     * 状态: ACTIVE-启用, INACTIVE-禁用
     */
    private String status;
}
