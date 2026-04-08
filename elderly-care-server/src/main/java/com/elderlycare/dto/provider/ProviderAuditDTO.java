package com.elderlycare.dto.provider;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 服务商审核DTO
 */
@Data
public class ProviderAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核状态: APPROVED已通过, REJECTED已拒绝
     */
    @NotBlank(message = "审核状态不能为空")
    private String auditStatus;

    /**
     * 审核备注
     */
    private String auditComment;
}
