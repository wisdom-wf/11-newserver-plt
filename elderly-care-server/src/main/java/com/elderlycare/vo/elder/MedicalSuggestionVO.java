package com.elderlycare.vo.elder;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 就医建议VO
 */
@Data
public class MedicalSuggestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 老人ID */
    private String elderId;

    /** 老人姓名 */
    private String elderName;

    /** 评估时间 */
    private String evaluateTime;

    /** 就医紧急程度 */
    private String urgencyLevel;

    /** 就医紧急程度名称 */
    private String urgencyLevelName;

    /** 就医科室建议 */
    private String suggestedDepartment;

    /** 就医建议列表 */
    private List<MedicalSuggestion> suggestions;

    /** 症状描述 */
    private List<String> symptoms;

    /**
     * 单条就医建议
     */
    @Data
    public static class MedicalSuggestion {
        /** 建议类型 */
        private String type;
        /** 建议类型名称 */
        private String typeName;
        /** 建议内容 */
        private String content;
        /** 优先级 */
        private Integer priority;
        /** 依据 */
        private String basis;
    }
}
