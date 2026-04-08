package com.elderlycare.vo.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 反馈VO
 */
@Data
public class FeedbackVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String feedbackId;

    private String feedbackType;

    private String feedbackTypeName;

    private String elderId;

    private String elderName;

    private String elderPhone;

    private String staffId;

    private String staffName;

    private String providerId;

    private String providerName;

    private String orderId;

    private String content;

    private String photos;

    private String handleStatus;

    private String handleStatusName;

    private String handlerId;

    private String handlerName;

    private String handlingResult;

    private LocalDateTime handleTime;

    private Integer status;

    private LocalDateTime createTime;
}
