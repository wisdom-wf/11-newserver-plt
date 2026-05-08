package com.elderlycare.entity.device;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_device")
public class Device implements Serializable {
    @TableId
    private String deviceId;
    private String deviceSn;
    private String deviceType;
    private String deviceName;
    private String manufacturer;
    private String model;
    private String status;
    private LocalDateTime lastPushTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
