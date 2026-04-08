package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 老人健康信息实体
 */
@Data
@TableName("t_elder_health")
public class ElderHealth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 健康信息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 身高(cm)
     */
    private Double height;

    /**
     * 体重(kg)
     */
    private Double weight;

    /**
     * 既往病史（JSON数组）
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
     * 慢性病：0-无，1-高血压，2-糖尿病，3-心脏病，4-脑血管疾病，5-慢阻肺，6-其他
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
     * 压疮风险：0-无，1-低风险，2-中风险，3-高风险
     */
    private Integer pressureSoreRisk;

    /**
     * 营养状态：0-正常，1-营养不良风险，2-营养不良
     */
    private Integer nutritionStatus;

    /**
     * 视力状况：0-正常，1-轻度障碍，2-中度障碍，3-重度障碍/失明
     */
    private Integer visionStatus;

    /**
     * 听力状况：0-正常，1-轻度障碍，2-中度障碍，3-重度障碍/失聪
     */
    private Integer hearingStatus;

    /**
     * 口腔健康：0-正常，1-有问题，2-严重问题
     */
    private Integer oralHealth;

    /**
     * 运动能力：0-正常，1-轻度受限，2-中度受限，3-重度受限
     */
    private Integer mobilityStatus;

    /**
     * 健康备注
     */
    private String healthRemark;

    /**
     * 体检日期
     */
    private LocalDateTime checkupDate;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private String updaterId;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
