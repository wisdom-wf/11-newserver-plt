package com.elderlycare.entity.quality;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质检实体
 */
@Data
@TableName("quality_check")
public class QualityCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 质检ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String qualityCheckId;

    /** 质检编号 */
    private String checkNo;

    /** 订单ID */
    private String orderId;

    /** 订单号 */
    private String orderNo;

    /** 服务日志ID */
    private String serviceLogId;

    /** 服务类别 */
    private String serviceCategory;

    /** 服务商ID */
    private String providerId;

    /** 服务商名称 */
    private String providerName;

    /** 服务人员ID */
    private String staffId;

    /** 服务人员姓名 */
    private String staffName;

    /** 质检类型：RANDOM/SCHEDULED/COMPLAINT/COMPLETION */
    private String checkType;

    /** 质检方式：PHOTO_REVIEW/PHONE_REVIEW/ON_SITE */
    private String checkMethod;

    /** 综合评分 */
    private BigDecimal checkScore;

    /** 质检结果：QUALIFIED/UNQUALIFIED/NEED_RECTIFY */
    private String checkResult;

    /** 质检照片 */
    private String checkPhotos;

    /** 质检备注 */
    private String checkRemark;

    /** 质检时间 */
    private LocalDateTime checkTime;

    /** 质检员ID */
    private String checkerId;

    /** 质检员姓名 */
    private String checkerName;

    /** 是否需要整改 */
    private Boolean needRectify;

    /** 整改通知 */
    private String rectifyNotice;

    /** 整改期限 */
    private LocalDateTime rectifyDeadline;

    /** 整改状态：PENDING/IN_PROGRESS/COMPLETED/VERIFIED/FAILED */
    private String rectifyStatus;

    /** 整改照片 */
    private String rectifyPhotos;

    /** 整改说明 */
    private String rectifyRemark;

    /** 复检时间 */
    private LocalDateTime recheckTime;

    /** 复检结果 */
    private String recheckResult;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleted;
}
