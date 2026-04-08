package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 派单记录VO
 */
@Data
public class DispatchVO implements Serializable {

    private String dispatchId;

    private String orderId;

    private String providerId;

    private String providerName;

    private String staffId;

    private String staffName;

    private LocalDateTime dispatchTime;

    private String dispatchType;

    private String dispatchStatus;

    private LocalDateTime receiveTime;

    private String receiveComment;

    private String rejectReason;

    private LocalDateTime createTime;
}
