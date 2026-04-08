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
     * 目标状态：0-待审核，1-正常，2-禁用，3-离职
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String auditRemark;
}
