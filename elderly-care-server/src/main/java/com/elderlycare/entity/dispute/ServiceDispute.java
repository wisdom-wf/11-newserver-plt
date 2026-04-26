package com.elderlycare.entity.dispute;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务争议实体
 */
@Data
@TableName("t_service_dispute")
public class ServiceDispute implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String disputeId;

    /**
     * 争议编号
     */
    private String disputeNo;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 争议类型：SERVICE_QUALITY-服务质量/DAMAGE-财产损失/REFUND-退款申请/OTHER-其他
     */
    private String disputeType;

    /**
     * 争议状态：APPLIED-已申请/INVESTIGATING-调查中/MEDIATING-调解中/AGREED-已协议/CLOSED-已关闭
     */
    private String disputeStatus;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 申请人电话
     */
    private String applicantPhone;

    /**
     * 申请内容
     */
    private String applicationContent;

    /**
     * 申请时间
     */
    private LocalDateTime appliedTime;

    /**
     * 调查结果
     */
    private String investigationContent;

    /**
     * 调查人ID
     */
    private String investigatorId;

    /**
     * 调查人姓名
     */
    private String investigatorName;

    /**
     * 调查时间
     */
    private LocalDateTime investigatedTime;

    /**
     * 调解内容
     */
    private String mediationContent;

    /**
     * 调解人ID
     */
    private String mediatorId;

    /**
     * 调解人姓名
     */
    private String mediatorName;

    /**
     * 调解时间
     */
    private LocalDateTime mediatedTime;

    /**
     * 协议内容
     */
    private String agreementContent;

    /**
     * 协议时间
     */
    private LocalDateTime agreedTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closedTime;

    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
