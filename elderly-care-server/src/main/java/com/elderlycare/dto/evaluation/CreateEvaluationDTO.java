package com.elderlycare.dto.evaluation;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 创建评价DTO
 */
@Data
public class CreateEvaluationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    /**
     * 服务人员ID
     */
    @NotBlank(message = "服务人员ID不能为空")
    private String staffId;

    /**
     * 总体评分(1-5)
     */
    @NotNull(message = "总体评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;

    /**
     * 态度评分(1-5)
     */
    @Min(value = 1, message = "态度评分最小为1")
    @Max(value = 5, message = "态度评分最大为5")
    private Integer attitudeScore;

    /**
     * 质量评分(1-5)
     */
    @Min(value = 1, message = "质量评分最小为1")
    @Max(value = 5, message = "质量评分最大为5")
    private Integer qualityScore;

    /**
     * 效率评分(1-5)
     */
    @Min(value = 1, message = "效率评分最小为1")
    @Max(value = 5, message = "效率评分最大为5")
    private Integer efficiencyScore;

    /**
     * 环境评分(1-5)
     */
    @Min(value = 1, message = "环境评分最小为1")
    @Max(value = 5, message = "环境评分最大为5")
    private Integer environmentScore;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签(多个逗号分隔)
     */
    private String tags;

    /**
     * 是否匿名(0-否,1-是)
     */
    private Integer anonymous = 0;

    /**
     * 关联质检单ID（可选，从质检详情跳转时带入）
     */
    private String qualityCheckId;

    /**
     * 关联服务日志ID（可选）
     */
    private String serviceLogId;
}
