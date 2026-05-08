package com.elderlycare.vo.device;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceVO {
    private String deviceId;
    private String deviceSn;
    private String deviceType;
    private String deviceName;
    private String manufacturer;
    private String model;
    private String status;
    private String statusText;
    private LocalDateTime lastPushTime;
    private LocalDateTime createTime;
}
