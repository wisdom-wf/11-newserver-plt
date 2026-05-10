package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康AI建议实体（数据库表 t_health_ai_suggestion）
 * 存储阿里云百炼生成的护理建议和就医建议，每日定时生成，前端直接读缓存
 */
@Data
@TableName("t_health_ai_suggestion")
public class HealthAiSuggestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 老人ID */
    private String elderId;

    /** 老人姓名（冗存，查询方便） */
    private String elderName;

    // ===== 护理建议字段 =====
    /** 护理等级建议 */
    private String careLevelSuggestion;

    /** 风险预警JSON数组 */
    private String riskAlerts;

    /** 护理建议JSON数组 */
    private String careSuggestions;

    /** 护理建议生成时间 */
    private LocalDateTime careEvaluateTime;

    // ===== 就医建议字段 =====
    /** 就医紧急程度：URGENT / WARNING / ATTENTION / NORMAL */
    private String urgencyLevel;

    /** 紧急程度名称 */
    private String urgencyLevelName;

    /** 建议就诊科室 */
    private String suggestedDepartment;

    /** 就医建议JSON数组 */
    private String medicalSuggestions;

    /** 异常症状JSON数组 */
    private String symptoms;

    /** 就医建议生成时间 */
    private LocalDateTime medicalEvaluateTime;

    // ===== AI模型信息 =====
    /** 调用的AI模型 */
    private String aiModel;

    /** Token消耗量 */
    private Integer aiTokensUsed;

    // ===== 语音播报 =====
    /** AI建议语音URL（qwen-tts合成，base64 data URI） */
    private String audioUrl;

    // ===== 元数据 =====
    /** AI生成时间 */
    private LocalDateTime generatedAt;

    /** 过期时间（生成时间+7天） */
    private LocalDateTime expiresAt;

    /** 状态：0-有效，1-已过期，2-生成失败 */
    private Integer status;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}