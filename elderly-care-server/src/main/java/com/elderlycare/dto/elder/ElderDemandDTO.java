package com.elderlycare.dto.elder;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 老人服务需求DTO
 */
@Data
public class ElderDemandDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 需求ID
     */
    private String demandId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 服务类型：0-生活照料，1-日间照料，2-助餐服务，3-助洁服务，4-助浴服务，5-健康监测，6-康复护理，7-精神慰藉，8-信息咨询，9-紧急救援
     */
    private Integer serviceType;

    /**
     * 服务项目（具体服务内容）
     */
    private String serviceItem;

    /**
     * 服务频率：0-每日，1-每周数次，2-每周一次，3-每月数次，4-按需
     */
    private Integer serviceFrequency;

    /**
     * 期望开始日期
     */
    private LocalDate expectStartDate;

    /**
     * 期望结束日期
     */
    private LocalDate expectEndDate;

    /**
     * 期望服务时间
     */
    private String expectServiceTime;

    /**
     * 期望服务地址
     */
    private String expectServiceAddress;

    /**
     * 特殊需求说明
     */
    private String specialRequirement;

    /**
     * 需求来源：0-本人申请，1-家属申请，2-社区转介，3-街道转介
     */
    private Integer demandSource;

    /**
     * 紧急程度：0-普通，1-紧急，2-非常紧急
     */
    private Integer urgencyLevel;

    /**
     * 状态：0-待评估，1-已评估待匹配，2-已匹配，3-服务中，4-已结束，5-已取消
     */
    private Integer status;

    /**
     * 评估人ID
     */
    private Long evaluatorId;

    /**
     * 评估时间
     */
    private String evaluateTime;

    /**
     * 评估结果备注
     */
    private String evaluateRemark;

    /**
     * 匹配的服务人员ID
     */
    private Long assignedStaffId;

    /**
     * 匹配的服务商ID
     */
    private Long assignedProviderId;
}
