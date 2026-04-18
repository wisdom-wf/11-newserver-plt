package com.elderlycare.vo.servicelog;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务日志VO
 */
@Data
public class ServiceLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务日志ID */
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

    /** 服务类别 */
    private String serviceCategory;

    /** 服务类型 */
    private String serviceType;

    /** 服务日期 */
    private String serviceDate;

    /** 服务开始时间 */
    private String serviceStartTime;

    /** 服务结束时间 */
    private String serviceEndTime;

    /** 服务时长（分钟） */
    private Integer serviceDuration;

    /** 实际服务时长 */
    private Integer actualDuration;

    /** 服务评价/内容 */
    private String serviceComment;

    /** 服务内容（前端字段名） */
    private String serviceContent;

    /** 服务状态 */
    private String status;

    /** 提交时间 */
    private String submitTime;

    /** 是否有异常 */
    private Boolean hasAnomaly;

    /** 异常类型 */
    private String anomalyType;

    /** 异常描述 */
    private String anomalyDesc;

    /** 异常处理状态 */
    private String anomalyStatus;

    /** 创建时间 */
    private String createTime;

    /** 服务照片（JSON数组格式） */
    private String[] servicePhotos;

    /** 审核备注 */
    private String reviewRemarks;

    /** 审核状态：DRAFT-草稿, SUBMITTED-已提交, APPROVED-已通过, REJECTED-已驳回 */
    private String auditStatus;

    /** 审核意见 */
    private String reviewComment;

    /** 审核人ID */
    private String reviewerId;

    /** 审核人姓名 */
    private String reviewerName;

    /** 审核时间 */
    private String reviewTime;
}
