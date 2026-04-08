package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;

/**
 * 签退DTO
 */
@Data
public class CheckOutDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 工作记录ID
     */
    private String recordId;

    /**
     * 签退地点纬度
     */
    private Double latitude;

    /**
     * 签退地点经度
     */
    private Double longitude;

    /**
     * 签退地点地址
     */
    private String address;

    /**
     * 签退设备
     */
    private String device;
}
