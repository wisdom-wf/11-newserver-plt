package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排班更新DTO
 */
@Data
public class ScheduleUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 班次类型：0-早班，1-中班，2-晚班，3-全天，4-休息
     */
    private Integer shiftType;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 排班类型：0-日常工作，1-加班，2-调休，3-请假
     */
    private Integer scheduleType;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 排班状态：0-已排班，1-已确认，2-已完成，3-已取消
     */
    private Integer status;

    /**
     * 排班备注
     */
    private String remark;
}
