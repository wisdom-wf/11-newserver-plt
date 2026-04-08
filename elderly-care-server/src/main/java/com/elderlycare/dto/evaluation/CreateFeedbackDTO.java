package com.elderlycare.dto.evaluation;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 创建反馈DTO
 */
@Data
public class CreateFeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈类型(COMPLAINT-投诉, SUGGESTION-建议, PRAISE-表扬, OTHER-其他)
     */
    @NotBlank(message = "反馈类型不能为空")
    private String feedbackType;

    /**
     * 老人ID
     */
    @NotBlank(message = "老人ID不能为空")
    private String elderId;

    /**
     * 服务人员ID(可选)
     */
    private String staffId;

    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    /**
     * 反馈图片(多个逗号分隔)
     */
    private String photos;

    /**
     * 订单ID(可选)
     */
    private String orderId;
}
