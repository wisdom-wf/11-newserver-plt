package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 补贴余额VO
 */
@Data
public class SubsidyBalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 补贴ID
     */
    private String subsidyId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 补贴类型名称
     */
    private String subsidyTypeName;

    /**
     * 补贴项目名称
     */
    private String subsidyName;

    /**
     * 补贴总额度
     */
    private BigDecimal totalQuota;

    /**
     * 已使用额度
     */
    private BigDecimal usedQuota;

    /**
     * 剩余额度
     */
    private BigDecimal remainingQuota;

    /**
     * 使用比例（百分比）
     */
    private BigDecimal usedRatio;

    /**
     * 补贴开始日期
     */
    private LocalDate startDate;

    /**
     * 补贴结束日期
     */
    private LocalDate endDate;

    /**
     * 状态：0-有效，1-已用完，2-已过期
     */
    private Integer status;
}
