package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单时间线VO
 */
@Data
public class OrderTimelineVO implements Serializable {

    private String status;

    private String statusName;

    private String description;

    private LocalDateTime time;

    private String operator;
}
