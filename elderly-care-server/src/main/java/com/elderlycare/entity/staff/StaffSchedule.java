package com.elderlycare.entity.staff;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 服务人员排班实体
 */
@Data
@TableName("t_staff_schedule")
public class StaffSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String scheduleId;

    /**
     * 服务人员ID
     */
    private String staffId;

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

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic(value = "1", delval = "0")
    private Integer deleted;

    /**
     * 创建者ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
