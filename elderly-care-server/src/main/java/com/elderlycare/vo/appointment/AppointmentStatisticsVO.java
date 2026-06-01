package com.elderlycare.vo.appointment;

import lombok.Data;

import java.io.Serializable;

/**
 * 预约统计VO
 */
@Data
public class AppointmentStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总数 */
    private Integer total = 0;

    /** 待处理数 */
    private Integer pending = 0;

    /** 已确认数 */
    private Integer confirmed = 0;

    /** 已分配数 */
    private Integer assigned = 0;

    /** 已完成数 */
    private Integer completed = 0;

    /** 已取消数 */
    private Integer cancelled = 0;

    /** 已作废数 */
    private Integer invalid = 0;
}
