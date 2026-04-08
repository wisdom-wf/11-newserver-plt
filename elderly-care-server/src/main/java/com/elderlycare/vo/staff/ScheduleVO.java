package com.elderlycare.vo.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 排班VO
 */
@Data
public class ScheduleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String scheduleId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 服务人员编号
     */
    private String staffNo;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 班次类型：0-早班，1-中班，2-晚班，3-全天，4-休息
     */
    private Integer shiftType;

    /**
     * 班次类型文本
     */
    private String shiftTypeText;

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
     * 排班类型文本
     */
    private String scheduleTypeText;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 排班状态：0-已排班，1-已确认，2-已完成，3-已取消
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 排班备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
