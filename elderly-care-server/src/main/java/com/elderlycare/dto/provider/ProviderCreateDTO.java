package com.elderlycare.dto.provider;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 服务商创建DTO
 */
@Data
public class ProviderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务商名称
     */
    @NotBlank(message = "服务商名称不能为空")
    private String providerName;

    /**
     * 服务商类型
     */
    @NotBlank(message = "服务商类型不能为空")
    private String providerType;

    /**
     * 服务类别: HOME_CARE-家政服务, ELDER_CARE-养老服务
     */
    private String serviceCategory;

    /**
     * 统一社会信用代码
     */
    @NotBlank(message = "统一社会信用代码不能为空")
    @Pattern(regexp = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$", message = "统一社会信用代码格式不正确")
    private String creditCode;

    /**
     * 法定代表人
     */
    @NotBlank(message = "法定代表人不能为空")
    private String legalPerson;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
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
}
