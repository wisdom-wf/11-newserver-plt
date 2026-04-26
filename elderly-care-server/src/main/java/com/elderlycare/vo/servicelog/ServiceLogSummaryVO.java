package com.elderlycare.vo.servicelog;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务日志摘要VO（供订单详情页批量展示，不含 photos 等大字段）
 * 字段全用 String，避免 LocalDateTime 类型转换问题
 */
@Data
public class ServiceLogSummaryVO implements Serializable {

    private String serviceLogId;

    /** 日志编号 */
    private String logNo;

    /** 审核状态：DRAFT-草稿, SUBMITTED-已提交, APPROVED-已通过, REJECTED-已驳回 */
    private String auditStatus;

    /** 服务状态：待服务/服务中/已完成 */
    private String serviceStatus;

    /** 服务日期 */
    private String serviceDate;

    /** 服务开始时间 */
    private String serviceStartTime;

    /** 服务结束时间 */
    private String serviceEndTime;

    /** 服务时长（分钟） */
    private Integer serviceDuration;

    /** 服务人员姓名 */
    private String staffName;

    /** 老人姓名 */
    private String elderName;

    /** 服务类型名称 */
    private String serviceTypeName;

    /** 创建时间 */
    private String createTime;

    /** 完成时间 */
    private String completionTime;

    /** 审核意见 */
    private String reviewComment;
}
