package com.elderlycare.dto.provider;

import lombok.Data;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 服务商更新DTO
 */
@Data
public class ProviderUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务商名称
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
     * 法定代表人
     */
    private String legalPerson;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    /**
     * 所在地址
     */
    private String address;

    /**
     * 服务区域
     */
    private String serviceAreas;

    /**
     * 简介
     */
    private String description;

    /**
     * 营业执照(base64)
     */
    private String businessLicense;

    /**
     * 状态: 0禁用, 1启用
     */
    private String status;
}
