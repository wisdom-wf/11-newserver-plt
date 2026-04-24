package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;

/**
 * 老人健康卡片VO（用于健康档案页面最近更新老人展示）
 */
@Data
public class ElderHealthCardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 头像URL
     */
    private String photoUrl;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别：MALE/FEMALE
     */
    private String gender;

    /**
     * 性别名称
     */
    private String genderName;

    /**
     * 护理等级：HIGH/MEDIUM/NORMAL
     */
    private String careLevel;

    /**
     * 护理等级名称
     */
    private String careLevelName;

    /**
     * 健康指数（0-100）
     */
    private Integer healthIndex;

    /**
     * 健康状态：NORMAL/WARNING/ALERT
     */
    private String healthStatus;

    /**
     * 最新测量值
     */
    private String latestMeasurementValue;

    /**
     * 最新测量时间
     */
    private String latestMeasurementTime;

    /**
     * 最新测量类型
     */
    private String latestMeasurementType;

    /**
     * 联系电话
     */
    private String phone;
}
