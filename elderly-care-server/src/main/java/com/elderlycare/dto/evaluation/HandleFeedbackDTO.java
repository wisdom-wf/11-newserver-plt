package com.elderlycare.dto.evaluation;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 处理反馈DTO
 */
@Data
public class HandleFeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理人ID
     */
    @NotBlank(message = "处理人ID不能为空")
    private String handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 处理结果
     */
    @NotBlank(message = "处理结果不能为空")
    private String handlingResult;
}
