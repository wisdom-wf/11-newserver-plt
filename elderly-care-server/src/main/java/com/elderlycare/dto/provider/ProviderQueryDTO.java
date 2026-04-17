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
     * 服务类别: HOME_CARE-家政服务, ELDER_CARE-养老服务
     */
    private String serviceCategory;

    /**
     * 状态
     */
    private String status;
}
