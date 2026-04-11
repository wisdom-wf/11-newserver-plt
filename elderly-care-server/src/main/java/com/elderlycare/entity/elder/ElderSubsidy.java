package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老人补贴信息实体
 */
@Data
@TableName("t_elder_subsidy")
public class ElderSubsidy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 补贴ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String subsidyId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 补贴类型：0-政府补贴，1-社区补贴，2-企业捐赠，3-其他
     */
    private Integer subsidyType;

    /**
     * 补贴项目名称
     */
    private String subsidyName;

    /**
     * 补贴标准金额
     */
    private BigDecimal standardAmount;

    /**
     * 补贴比例（百分比）
     */
    private BigDecimal subsidyRatio;

    /**
     * 实际补贴金额
     */
    private BigDecimal actualSubsidyAmount;

    /**
     * 自付金额
     */
    private BigDecimal selfPayAmount;

    /**
     * 补贴开始日期
     */
    private LocalDate startDate;

    /**
     * 补贴结束日期
     */
    private LocalDate endDate;

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
     * 申请日期
     */
    private LocalDate applyDate;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核不通过，3-已取消
     */
    @TableField("subsidy_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private String auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 补贴发放状态：0-未发放，1-发放中，2-已发放，3-发放失败
     */
    private Integer grantStatus;

    /**
     * 最后发放日期
     */
    private LocalDate lastGrantDate;

    /**
     * 下次发放日期
     */
    private LocalDate nextGrantDate;

    /**
     * 发放周期：0-月度，1-季度，2-年度
     */
    private Integer grantCycle;

    /**
     * 政策依据
     */
    private String policyBasis;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private String updaterId;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
