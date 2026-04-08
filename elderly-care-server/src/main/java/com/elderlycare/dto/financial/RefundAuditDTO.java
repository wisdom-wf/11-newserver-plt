package com.elderlycare.dto.financial;

import lombok.Data;
import java.io.Serializable;

/**
 * 退款审核DTO
 */
@Data
public class RefundAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auditStatus;

    private String auditComment;

    private String remark;
}
