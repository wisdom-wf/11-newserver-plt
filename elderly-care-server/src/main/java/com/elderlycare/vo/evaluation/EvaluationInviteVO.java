package com.elderlycare.vo.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价邀请VO
 */
@Data
public class EvaluationInviteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private String evaluationId;

    /**
     * 评价邀请Token
     */
    private String token;

    /**
     * 评价邀请链接
     */
    private String surveyUrl;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务商名称
     */
    private String providerName;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * Token状态：PENDING-待评价，COMPLETED-已评价，EXPIRED-已过期
     */
    private String tokenStatus;

    /**
     * Token过期时间
     */
    private LocalDateTime tokenExpireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
