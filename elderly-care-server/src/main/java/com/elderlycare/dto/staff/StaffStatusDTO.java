package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;

/**
 * 服务人员状态变更DTO
 */
@Data
public class StaffStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标状态：PENDING-待审核，ON_JOB-正常，OFF_JOB-离职
     */
    private String status;

    /**
     * 审核备注
     */
    private String auditRemark;
}
