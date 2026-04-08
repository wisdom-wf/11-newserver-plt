package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 老人健康信息VO
 */
@Data
public class ElderHealthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 健康信息ID
     */
    private String healthId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 血型：0-A型，1-B型，2-O型，3-AB型
     */
    private Integer bloodType;

    /**
     * 血型名称
     */
    private String bloodTypeName;

    /**
     * 身高(cm)
     */
    private Double height;

    /**
     * 体重(kg)
     */
    private Double weight;

    /**
     * 既往病史
     */
    private String medicalHistory;

    /**
     * 当前用药情况
     */
    private String currentMedication;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 慢性病
     */
    private String chronicDiseases;

    /**
     * 日常生活能力评分（ADL）
     */
    private Integer adlScore;

    /**
     * 认知能力评分（MMSE）
     */
    private Integer mmseScore;

    /**
     * 抑郁评分（GDS）
     */
    private Integer gdsScore;

    /**
     * 跌倒风险：0-低风险，1-中风险，2-高风险
     */
    private Integer fallRisk;

    /**
     * 跌倒风险名称
     */
    private String fallRiskName;

    /**
     * 压疮风险：0-无，1-低风险，2-中风险，3-高风险
     */
    private Integer pressureSoreRisk;

    /**
     * 压疮风险名称
     */
    private String pressureSoreRiskName;

    /**
     * 营养状态：0-正常，1-营养不良风险，2-营养不良
     */
    private Integer nutritionStatus;

    /**
     * 营养状态名称
     */
    private String nutritionStatusName;

    /**
     * 视力状况：0-正常，1-轻度障碍，2-中度障碍，3-重度障碍/失明
     */
    private Integer visionStatus;

    /**
     * 视力状况名称
     */
    private String visionStatusName;

    /**
     * 听力状况：0-正常，1-轻度障碍，2-中度障碍，3-重度障碍/失聪
     */
    private Integer hearingStatus;

    /**
     * 听力状况名称
     */
    private String hearingStatusName;

    /**
     * 口腔健康：0-正常，1-有问题，2-严重问题
     */
    private Integer oralHealth;

    /**
     * 口腔健康名称
     */
    private String oralHealthName;

    /**
     * 运动能力：0-正常，1-轻度受限，2-中度受限，3-重度受限
     */
    private Integer mobilityStatus;

    /**
     * 运动能力名称
     */
    private String mobilityStatusName;

    /**
     * 健康备注
     */
    private String healthRemark;

    /**
     * 体检日期
     */
    private LocalDateTime checkupDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
