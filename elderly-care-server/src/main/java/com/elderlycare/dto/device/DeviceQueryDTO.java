package com.elderlycare.dto.device;

import lombok.Data;

@Data
public class DeviceQueryDTO {
    private String deviceType;
    private String status;
    private String elderId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
