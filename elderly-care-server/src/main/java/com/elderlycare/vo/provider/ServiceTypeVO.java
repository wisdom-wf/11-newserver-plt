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

    private String providerServiceId;
    private String providerId;
    private String serviceTypeCode;
    private String serviceTypeName;
    private BigDecimal subsidyPrice;
    private BigDecimal servicePrice;
    private String serviceArea;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
