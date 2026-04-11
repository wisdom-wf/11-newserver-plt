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
    private Integer total;

    /** 待处理数 */
    private Integer pending;

    /** 已确认数 */
    private Integer confirmed;

    /** 已分配数 */
    private Integer assigned;

    /** 已完成数 */
    private Integer completed;

    /** 已取消数 */
    private Integer cancelled;

    /** 已作废数 */
    private Integer invalid;
}
