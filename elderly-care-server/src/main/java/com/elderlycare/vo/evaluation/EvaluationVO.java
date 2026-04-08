package com.elderlycare.vo.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价VO
 */
@Data
public class EvaluationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String evaluationId;

    private String orderId;

    private String staffId;

    private String staffName;

    private String providerId;

    private String providerName;

    private String elderId;

    private String elderName;

    private String serviceTypeCode;

    private String serviceTypeName;

    private Integer rating;

    private Integer attitudeScore;

    private Integer qualityScore;

    private Integer efficiencyScore;

    private String content;

    private String tags;

    private Integer anonymous;

    private Integer status;

    private String replyContent;

    private LocalDateTime replyTime;

    private LocalDateTime createTime;
}
