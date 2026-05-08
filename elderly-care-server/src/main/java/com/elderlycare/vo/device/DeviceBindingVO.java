package com.elderlycare.vo.device;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceBindingVO {
    private String bindingId;
    private String deviceId;
    private String deviceSn;
    private String deviceType;
    private String deviceTypeName;
    private String deviceName;
    private String elderId;
    private String elderName;
    private String measurementType;
    private String measurementTypeName;
    private LocalDateTime bindTime;
    private String status;
    private String deviceStatus;
    private LocalDateTime lastPushTime;
    private String remark;
}
