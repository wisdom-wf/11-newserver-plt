package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;

/**
 * 签到DTO
 */
@Data
public class CheckInDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 签到地点纬度
     */
    private Double latitude;

    /**
     * 签到地点经度
     */
    private Double longitude;

    /**
     * 签到地点地址
     */
    private String address;

    /**
     * 签到设备
     */
    private String device;
}
