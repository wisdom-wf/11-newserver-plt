package com.elderlycare.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO implements Serializable {

    @NotBlank(message = "客户姓名不能为空")
    private String elderName;

    @NotBlank(message = "手机号不能为空")
    private String elderPhone;

    @NotBlank(message = "服务类型不能为空")
    private String serviceTypeCode;

    @NotNull(message = "服务日期不能为空")
    private LocalDate serviceDate;

    @NotBlank(message = "服务时间不能为空")
    private String serviceTime;

    @NotNull(message = "服务时长不能为空")
    private Integer serviceDuration;

    @NotBlank(message = "服务地址不能为空")
    private String serviceAddress;

    private String elderId;

    private String serviceTypeName;

    private String specialRequirements;

    private String orderType;

    private String orderSource;

    private String subsidyType;

    private BigDecimal estimatedPrice;

    private BigDecimal subsidyAmount;

    private BigDecimal selfPayAmount;
}
