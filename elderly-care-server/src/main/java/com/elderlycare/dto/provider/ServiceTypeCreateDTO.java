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
    private String serviceTypeCode;

    /**
     * 服务类型名称
     */
    @NotBlank(message = "服务类型名称不能为空")
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
}
