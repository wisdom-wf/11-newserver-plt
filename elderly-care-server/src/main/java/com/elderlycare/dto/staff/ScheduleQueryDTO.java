package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 排班查询DTO
 */
@Data
public class ScheduleQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 排班日期
     */
    private LocalDate date;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 班次类型：0-早班，1-中班，2-晚班，3-全天，4-休息
     */
    private Integer shiftType;

    /**
     * 排班状态：0-已排班，1-已确认，2-已完成，3-已取消
     */
    private Integer status;
}
