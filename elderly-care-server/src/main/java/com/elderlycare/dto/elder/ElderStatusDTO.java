package com.elderlycare.dto.elder;

import lombok.Data;
import java.io.Serializable;

/**
 * 老人状态变更DTO
 */
@Data
public class ElderStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 状态：0-待审核，1-正常，2-暂停服务，3-注销
     */
    private String status;

    /**
     * 审核备注
     */
    private String auditRemark;
}
