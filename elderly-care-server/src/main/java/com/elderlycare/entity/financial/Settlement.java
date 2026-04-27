package com.elderlycare.entity.financial;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结算单实体
 */
@Data
@TableName("t_settlement")
public class Settlement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String settlementId;

    // DB无此列
    @TableField(exist = false)
    private String settlementNo;

    @TableField(exist = false)
    private String settlementType;

    private String providerId;

    // DB无此列
    @TableField(exist = false)
    private String providerName;

    private String staffId;

    // DB无此列
    @TableField(exist = false)
    private String staffName;

    private String orderId;

    // 联表/不存在字段
    @TableField(exist = false)
    private String elderId;

    @TableField(exist = false)
    private LocalDate serviceDate;

    @TableField(exist = false)
    private Integer serviceDuration;

    @TableField(exist = false)
    private BigDecimal unitPrice;

    // DB无此列
    @TableField(exist = false)
    private LocalDate settlementPeriodStart;

    @TableField(exist = false)
    private LocalDate settlementPeriodEnd;

    @TableField(exist = false)
    private Integer totalOrderCount;

    @TableField("total_amount")
    private BigDecimal totalServiceAmount;

    @TableField("subsidy_amount")
    private BigDecimal totalSubsidyAmount;

    @TableField("self_pay_amount")
    private BigDecimal totalSelfPayAmount;

    @TableField(exist = false)
    private BigDecimal settlementAmount;

    @TableField("payment_status")
    private String status;

    private String paymentMethod;

    private LocalDateTime paymentTime;

    private String invoiceStatus;

    private String transactionId;

    private LocalDateTime settlementTime;

    // 联表/不存在字段
    @TableField(exist = false)
    private String confirmTime;

    @TableField(exist = false)
    private String confirmUserId;

    @TableField(exist = false)
    private String confirmUserName;

    @TableField(exist = false)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // DB无update_time列，但Service层需要
    @TableField(exist = false)
    private LocalDateTime updateTime;

}
