package com.elderlycare.entity.device;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_device_binding")
public class DeviceBinding implements Serializable {
    @TableId
    private String bindingId;
    private String deviceId;
    private String elderId;
    private String measurementType;
    private LocalDateTime bindTime;
    private LocalDateTime unbindTime;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
