package com.elderlycare.entity.appointment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约管理实体
 */
@Data
@TableName("appointment")
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 预约ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String appointmentId;

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

    /** 预约状态：PENDING/CONFIRMED/ASSIGNED/IN_SERVICE/COMPLETED/CANCELLED/INVALID */
    private String status;

    /** 数据有效性：VALID/INVALID */
    private String validity;

    /** 作废原因 */
    private String cancelReason;

    /** 回复信息 */
    private String replyInfo;

    /** 评估类型 */
    private String assessmentType;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 确认时间 */
    private LocalDateTime confirmTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
