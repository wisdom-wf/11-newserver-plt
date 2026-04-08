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

    private String settlementNo;

    private String settlementType;

    private String providerId;

    private String providerName;

    private String staffId;

    private String staffName;

    private LocalDate settlementPeriodStart;

    private LocalDate settlementPeriodEnd;

    private Integer totalOrderCount;

    private BigDecimal totalServiceAmount;

    private BigDecimal totalSubsidyAmount;

    private BigDecimal totalSelfPayAmount;

    private BigDecimal settlementAmount;

    private String status;

    private String confirmTime;

    private String confirmUserId;

    private String confirmUserName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
