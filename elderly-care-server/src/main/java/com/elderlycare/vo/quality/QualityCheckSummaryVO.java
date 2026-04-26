package com.elderlycare.vo.quality;

import lombok.Data;
import java.io.Serializable;

/**
 * 质检摘要VO（供订单详情页批量展示）
 * 字段全用 String，避免 LocalDateTime 类型转换问题
 */
@Data
public class QualityCheckSummaryVO implements Serializable {

    private String qualityCheckId;

    /** 质检编号 */
    private String checkNo;

    /** 质检类型：RANDOM/SCHEDULED/COMPLAINT/COMPLETION */
    private String checkType;

    /** 关联的服务日志ID */
    private String serviceLogId;

    /** 质检结果：PENDING-待质检, QUALIFIED-合格, UNQUALIFIED-不合格, NEED_RECTIFY-需整改 */
    private String checkResult;

    /** 服务人员姓名 */
    private String staffName;

    /** 质检员姓名 */
    private String checkerName;

    /** 是否需整改 */
    private Boolean needRectify;

    /** 整改状态：PENDING/IN_PROGRESS/RECHECK/COMPLETED/VERIFIED/FAILED */
    private String rectifyStatus;

    /** 质检时间 */
    private String checkTime;

    /** 创建时间 */
    private String createTime;
}
