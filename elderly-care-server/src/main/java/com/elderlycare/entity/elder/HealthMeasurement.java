package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康测量记录实体
 */
@Data
@TableName("t_health_measurement")
public class HealthMeasurement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 测量记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String measurementId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 服务日志ID（可选，关联服务）
     */
    private String serviceLogId;

    /**
     * 测量类型：BLOOD_PRESSURE-血压, BLOOD_GLUCOSE-血糖, WEIGHT-体重,
     * TEMPERATURE-体温, PULSE-脉搏, SPO2-血氧, PAIN_SCALE-疼痛指数, OTHER-其他
     */
    private String measurementType;

    /**
     * 测量值
     */
    private String measurementValue;

    /**
     * 单位
     */
    private String measurementUnit;

    /**
     * 测量时间
     */
    private LocalDateTime measuredAt;

    /**
     * 测量人员ID
     */
    private String staffId;

    /**
     * 测量人员姓名
     */
    private String staffName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
