package com.elderlycare.dto.appointment;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 预约创建DTO
 */
@Data
public class AppointmentCreateDTO {

    /** 老人姓名 */
    @NotBlank(message = "老人姓名不能为空")
    private String elderName;

    /** 老人身份证号 */
    private String elderIdCard;

    /** 老人手机号 */
    @NotBlank(message = "老人手机号不能为空")
    private String elderPhone;

    /** 老人地址 */
    private String elderAddress;

    /** 区域ID */
    private String elderAreaId;

    /** 区域名称 */
    private String elderAreaName;

    /** 预约服务类型 */
    @NotBlank(message = "服务类型不能为空")
    private String serviceType;

    /** 服务类型编码 */
    private String serviceTypeCode;

    /** 服务内容类型 */
    private String serviceContent;

    /** 预约时间 */
    @NotBlank(message = "预约时间不能为空")
    private String appointmentTime;

    /** 预计服务时长（分钟） */
    private Integer serviceDuration;

    /** 服务机构ID */
    private String providerId;

    /** 服务机构名称 */
    private String providerName;

    /** 来访人数 */
    private Integer visitorCount;

    /** 备注 */
    private String remark;

    /** 评估类型 */
    private String assessmentType;
}
