package com.elderlycare.entity.device;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_device_push_log")
public class DevicePushLog implements Serializable {
    @TableId
    private String pushId;
    private String deviceId;
    private String bindingId;
    private String elderId;
    private String rawData;
    private String measurementId;
    private LocalDateTime pushTime;
    private String processStatus;
    private String errorMsg;
}
