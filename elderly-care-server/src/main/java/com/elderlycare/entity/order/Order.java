package com.elderlycare.entity.order;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String orderId;

    private String orderNo;

    private String elderId;

    private String elderName;

    private String elderPhone;

    private String serviceTypeCode;

    private String serviceTypeName;

    private LocalDate serviceDate;

    private String serviceTime;

    private Integer serviceDuration;

    private String serviceAddress;

    private String specialRequirements;

    private String orderType;

    private String orderSource;

    private String subsidyType;

    private BigDecimal estimatedPrice;

    private BigDecimal subsidyAmount;

    private BigDecimal selfPayAmount;

    private String status;

    private String providerId;

    private String staffId;

    private String cancelReason;

    private LocalDateTime dispatchTime;

    private LocalDateTime receiveTime;

    private LocalDateTime startTime;

    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
