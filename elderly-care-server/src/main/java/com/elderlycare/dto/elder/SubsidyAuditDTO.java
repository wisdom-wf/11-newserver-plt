package com.elderlycare.dto.elder;

import lombok.Data;
import java.io.Serializable;

/**
 * 补贴审核DTO
 */
@Data
public class SubsidyAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 补贴ID
     */
    private String subsidyId;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核不通过，3-已取消
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;
}
