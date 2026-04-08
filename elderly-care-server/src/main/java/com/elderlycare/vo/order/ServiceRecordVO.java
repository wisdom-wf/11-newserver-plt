package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务记录VO
 */
@Data
public class ServiceRecordVO implements Serializable {

    private String serviceRecordId;

    private String orderId;

    private String orderNo;

    private String staffId;

    private String staffName;

    private String elderName;

    private String serviceTypeName;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Integer serviceDuration;

    private String checkInLocation;

    private String checkOutLocation;

    private List<String> servicePhotos;

    private String serviceVideo;

    private String serviceLog;

    private String abnormalSituation;

    private String elderSignature;

    private String serviceStatus;

    private LocalDateTime createTime;
}
