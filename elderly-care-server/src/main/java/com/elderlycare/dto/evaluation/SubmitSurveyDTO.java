package com.elderlycare.dto.evaluation;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 提交问卷评价DTO
 */
@Data
public class SubmitSurveyDTO {

    /**
     * 服务评分(1-5)
     */
    @NotNull(message = "服务评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer serviceScore;

    /**
     * 态度评分(1-5)
     */
    @NotNull(message = "态度评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer attitudeScore;

    /**
     * 技能评分(1-5)
     */
    @NotNull(message = "技能评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer skillScore;

    /**
     * 准时评分(1-5)
     */
    @NotNull(message = "准时评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer punctualityScore;

    /**
     * 环境评分(1-5)
     */
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer environmentScore;

    /**
     * 满意度等级
     */
    private String satisfactionLevel;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签
     */
    private List<String> tags;

    /**
     * 评价图片
     */
    private List<String> images;

    /**
     * 是否匿名
     */
    private Boolean anonymous = false;
}
