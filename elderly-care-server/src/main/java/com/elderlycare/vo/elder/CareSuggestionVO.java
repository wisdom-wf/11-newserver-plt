package com.elderlycare.vo.elder;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 护理建议VO
 */
@Data
public class CareSuggestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 老人ID */
    private String elderId;

    /** 老人姓名 */
    private String elderName;

    /** 评估时间 */
    private String evaluateTime;

    /** 整体护理等级建议 */
    private String careLevelSuggestion;

    /** 护理建议列表 */
    private List<CareSuggestion> suggestions;

    /** 风险提示 */
    private List<String> riskAlerts;

    /**
     * 单条护理建议
     */
    @Data
    public static class CareSuggestion {
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
