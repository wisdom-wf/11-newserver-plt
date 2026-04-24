package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康测量记录VO
 */
@Data
public class HealthMeasurementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 测量记录ID
     */
    private String measurementId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 服务日志ID
     */
    private String serviceLogId;

    /**
     * 测量类型
     */
    private String measurementType;

    /**
     * 测量类型名称
     */
    private String measurementTypeName;

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
    private LocalDateTime createTime;
}
