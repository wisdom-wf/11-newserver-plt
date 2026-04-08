package com.elderlycare.dto.evaluation;

import lombok.Data;
import java.io.Serializable;

/**
 * 反馈查询DTO
 */
@Data
public class FeedbackQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 反馈类型(COMPLAINT-投诉, SUGGESTION-建议, PRAISE-表扬, OTHER-其他)
     */
    private String feedbackType;

    /**
     * 处理状态(PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, REJECTED-已驳回)
     */
    private String handleStatus;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
