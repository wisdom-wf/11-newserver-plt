package com.elderlycare.entity.servicelog;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务日志实体
 */
@Data
@TableName("service_log")
public class ServiceLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务日志ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String serviceLogId;

    /** 服务日志编号 */
    private String logNo;

    /** 订单ID */
    private String orderId;

    /** 订单号 */
    private String orderNo;

    /** 老人ID */
    private String elderId;

    /** 老人姓名 */
    private String elderName;

    /** 老人手机号 */
    private String elderPhone;

    /** 老人地址 */
    private String elderAddress;

    /** 服务人员ID */
    private String staffId;

    /** 服务人员姓名 */
    private String staffName;

    /** 服务人员手机号 */
    private String staffPhone;

    /** 服务商ID */
    private String providerId;

    /** 服务商名称 */
    private String providerName;

    /** 服务类型编码 */
    private String serviceTypeCode;

    /** 服务类型名称 */
    private String serviceTypeName;

    /** 服务日期 */
    private String serviceDate;

    /** 服务开始时间 */
    private LocalDateTime serviceStartTime;

    /** 服务结束时间 */
    private LocalDateTime serviceEndTime;

    /** 服务时长（分钟） */
    private Integer serviceDuration;

    /** 服务状态 */
    private String serviceStatus;

    /** 实际服务时长 */
    private Integer actualDuration;

    /** 服务评分 */
    private BigDecimal serviceScore;

    /** 服务评价 */
    private String serviceComment;

    /** 服务照片 */
    private String servicePhotos;

    /** 老人签名 */
    private String elderSignature;

    /** 异常类型 */
    private String anomalyType;

    /** 异常描述 */
    private String anomalyDesc;

    /** 异常照片 */
    private String anomalyPhotos;

    /** 异常处理状态 */
    private String anomalyStatus;

    /** 审核备注 */
    private String reviewRemarks;

    /** 审核状态: PENDING-待审核, APPROVED-已通过, REJECTED-已驳回 */
    private String auditStatus;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
