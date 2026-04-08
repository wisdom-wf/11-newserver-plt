package com.elderlycare.entity.financial;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务定价实体
 */
@Data
@TableName("t_service_price")
public class ServicePrice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String priceId;

    private String serviceTypeCode;

    private String serviceTypeName;

    private String providerId;

    private String providerName;

    private BigDecimal governmentPrice;

    private BigDecimal selfPayPrice;

    private BigDecimal totalPrice;

    private String priceType;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
