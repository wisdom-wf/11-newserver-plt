package com.elderlycare.entity.order;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务记录实体
 */
@Data
@TableName("t_service_record")
public class ServiceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String serviceRecordId;

    private String orderId;

    private String staffId;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Integer serviceDuration;

    private String checkInLocation;

    private String checkOutLocation;

    private String servicePhotos;

    private String serviceVideo;

    private String serviceLog;

    private String abnormalSituation;

    private String elderSignature;

    private String serviceStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
