package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作记录查询DTO
 */
@Data
public class WorkRecordQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 签到状态：0-正常，1-迟到，2-早退，3-旷工
     */
    private Integer checkInStatus;

    /**
     * 记录状态：0-正常，1-异常
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
