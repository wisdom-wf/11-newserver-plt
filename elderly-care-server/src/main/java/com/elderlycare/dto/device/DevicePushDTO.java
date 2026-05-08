package com.elderlycare.dto.device;

import lombok.Data;
import java.util.Map;

@Data
public class DevicePushDTO {
    private String deviceSn;
    private Long timestamp;
    private Map<String, Object> data;
}
