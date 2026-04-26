package com.elderlycare.dto.quality;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质检执行DTO（POST /api/quality-check/{id}/inspect）
 */
@Data
public class InspectionDTO {

    /** 质检评分（0-100） */
    private BigDecimal checkScore;

    /** 质检方式：PHOTO_REVIEW/PHONE_REVIEW/ON_SITE */
    private String checkMethod;

    /** 质检结论：QUALIFIED-合格, UNQUALIFIED-不合格, NEED_RECTIFY-需整改 */
    private String checkResult;

    /** 质检照片（逗号分隔的URL） */
    private String checkPhotos;

    /** 质检备注 */
    private String checkRemark;

    /** 整改通知（当 checkResult=NEED_RECTIFY 时必填） */
    private String rectifyNotice;

    /** 整改期限（当 checkResult=NEED_RECTIFY 时必填） */
    private LocalDateTime rectifyDeadline;
}
