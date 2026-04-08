package com.elderlycare.vo.provider;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务类型视图对象
 */
@Data
public class ServiceTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceTypeId;
    private String providerId;
    private String serviceCode;
    private String serviceName;
    private String description;
    private BigDecimal subsidyPrice;
    private BigDecimal marketPrice;
    private String unit;
    private Integer estimatedDuration;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
