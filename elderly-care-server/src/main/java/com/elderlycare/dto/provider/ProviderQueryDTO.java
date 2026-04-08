package com.elderlycare.dto.provider;

import lombok.Data;
import java.io.Serializable;

/**
 * 服务商查询DTO
 */
@Data
public class ProviderQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 服务商名称(模糊查询)
     */
    private String providerName;

    /**
     * 服务商类型
     */
    private String providerType;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 状态
     */
    private Integer status;
}
