package com.elderlycare.entity.staff;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务人员工作记录实体（签到签退）
 */
@Data
@TableName("t_staff_work_record")
public class StaffWorkRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String recordId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 关联排班ID
     */
    private String scheduleId;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 签到地点纬度
     */
    private Double checkInLatitude;

    /**
     * 签到地点经度
     */
    private Double checkInLongitude;

    /**
     * 签到地点地址
     */
    private String checkInAddress;

    /**
     * 签到设备
     */
    private String checkInDevice;

    /**
     * 签退时间
     */
    private LocalDateTime checkOutTime;

    /**
     * 签退地点纬度
     */
    private Double checkOutLatitude;

    /**
     * 签退地点经度
     */
    private Double checkOutLongitude;

    /**
     * 签退地点地址
     */
    private String checkOutAddress;

    /**
     * 签退设备
     */
    private String checkOutDevice;

    /**
     * 工作时长（分钟）
     */
    private Integer workDuration;

    /**
     * 签到状态：0-正常，1-迟到，2-早退，3-旷工
     */
    private Integer checkInStatus;

    /**
     * 签退状态：0-正常，1-迟到，2-早退
     */
    private Integer checkOutStatus;

    /**
     * 记录状态：0-正常，1-异常
     */
    private Integer status;

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

    /**
     * 备注
     */
    private String remark;
}
