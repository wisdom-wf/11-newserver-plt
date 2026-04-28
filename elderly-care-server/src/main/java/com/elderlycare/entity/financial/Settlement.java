package com.elderlycare.entity.financial;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_settlement")
public class Settlement implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String settlementId;

    private String settlementNo;

    private String providerId;

    private String staffId;

    private String orderId;

    // 联表字段
    @TableField(exist = false)
    private String providerName;

    @TableField(exist = false)
    private String staffName;

    // DB 字段
    private String elderId;

    @TableField(exist = false)
    private String elderName;

    private LocalDate serviceDate;

    // 联表字段（前端/VO 期望）
    @TableField(exist = false)
    private String orderNo;

    // 联表字段（前端/VO 期望）
    @TableField(exist = false)
    private String settlementType;

    @TableField("total_amount")
    private BigDecimal totalServiceAmount;

    @TableField("subsidy_amount")
    private BigDecimal totalSubsidyAmount;

    @TableField("self_pay_amount")
    private BigDecimal totalSelfPayAmount;

    private BigDecimal unitPrice;

    // DB 列名是 payment_status，映射到 paymentStatus
    @TableField("payment_status")
    private String paymentStatus;

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

    // 联表/不存在字段
    @TableField(exist = false)
    private LocalDateTime updateTime;

    // 联表/不存在字段
    @TableField(exist = false)
    private LocalDate settlementPeriodStart;

    @TableField(exist = false)
    private LocalDate settlementPeriodEnd;

    // 联表/不存在字段
    @TableField(exist = false)
    private Integer totalOrderCount;

    // 联表/不存在字段
    @TableField(exist = false)
    private BigDecimal settlementAmount;
}
