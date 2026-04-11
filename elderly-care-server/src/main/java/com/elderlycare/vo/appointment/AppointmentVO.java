package com.elderlycare.vo.appointment;

import lombok.Data;

import java.io.Serializable;

/**
 * 预约VO
 */
@Data
public class AppointmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 预约ID */
    private String appointmentId;

    /** 预约ID (兼容前端) */
    private String id;

    /** 预约单号 */
    private String appointmentNo;

    /** 老人姓名 */
    private String elderName;

    /** 老人身份证号 */
    private String elderIdCard;

    /** 老人手机号 */
    private String elderPhone;

    /** 老人地址 */
    private String elderAddress;

    /** 区域ID */
    private String elderAreaId;

    /** 区域名称 */
    private String elderAreaName;

    /** 预约服务类型 */
    private String serviceType;

    /** 服务类型编码 */
    private String serviceTypeCode;

    /** 服务内容类型 */
    private String serviceContent;

    /** 预约时间 */
    private String appointmentTime;

    /** 预计服务时长（分钟） */
    private Integer serviceDuration;

    /** 服务机构ID */
    private String providerId;

    /** 服务机构名称 */
    private String providerName;

    /** 服务机构地址 */
    private String providerAddress;

    /** 来访人数 */
    private Integer visitorCount;

    /** 备注 */
    private String remark;

    /** 预约状态 */
    private String status;

    /** 数据有效性 */
    private String validity;

    /** 作废原因 */
    private String cancelReason;

    /** 回复信息 */
    private String replyInfo;

    /** 评估类型 */
    private String assessmentType;

    /** 创建时间 */
    private String createTime;

    /** 确认时间 */
    private String confirmTime;

    /** 更新时间 */
    private String updateTime;
}
