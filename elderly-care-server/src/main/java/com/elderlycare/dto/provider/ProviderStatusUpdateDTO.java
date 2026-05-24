package com.elderlycare.dto.provider;

import lombok.Data;
import java.io.Serializable;

/**
 * 服务商状态更新DTO
 */
@Data
public class ProviderStatusUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态值: ENABLED-正常, DISABLED-禁用, DEMOTED-已降级, ELIMINATED-已淘汰
     */
    private String status;
}