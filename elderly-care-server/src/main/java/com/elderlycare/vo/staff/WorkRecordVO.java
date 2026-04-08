package com.elderlycare.vo.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作记录VO
 */
@Data
public class WorkRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String recordId;

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
     * 签到状态文本
     */
    private String checkInStatusText;

    /**
     * 签退状态：0-正常，1-迟到，2-早退
     */
    private Integer checkOutStatus;

    /**
     * 签退状态文本
     */
    private String checkOutStatusText;

    /**
     * 记录状态：0-正常，1-异常
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 备注
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
