package com.elderlycare.dto.provider;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务类型创建DTO
 */
@Data
public class ServiceTypeCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型编码
     */
    @NotBlank(message = "服务类型编码不能为空")
    private String serviceCode;

    /**
     * 服务类型名称
     */
    @NotBlank(message = "服务类型名称不能为空")
    private String serviceName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 政府补贴价格
     */
    @NotNull(message = "政府补贴价格不能为空")
    private BigDecimal subsidyPrice;

    /**
     * 市场价格
     */
    @NotNull(message = "市场价格不能为空")
    private BigDecimal marketPrice;

    /**
     * 单位
     */
    @NotBlank(message = "单位不能为空")
    private String unit;

    /**
     * 预计服务时长(分钟)
     */
    private Integer estimatedDuration;
}
