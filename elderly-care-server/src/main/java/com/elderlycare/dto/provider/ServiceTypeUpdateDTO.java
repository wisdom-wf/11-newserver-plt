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
    private String serviceName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 政府补贴价格
     */
    private BigDecimal subsidyPrice;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 单位
     */
    private String unit;

    /**
     * 预计服务时长(分钟)
     */
    private Integer estimatedDuration;

    /**
     * 状态: 0禁用, 1启用
     */
    private Integer status;
}
