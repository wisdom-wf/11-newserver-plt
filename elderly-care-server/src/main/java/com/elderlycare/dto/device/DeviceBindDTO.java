package com.elderlycare.dto.device;

import lombok.Data;

@Data
public class DeviceBindDTO {
    private String deviceSn;
    private String elderId;
    private String measurementType;
    private String remark;
}
