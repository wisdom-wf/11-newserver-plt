package com.elderlycare.vo.appointment;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 预约时间轴VO
 */
@Data
public class AppointmentTimelineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 预约ID */
    private String appointmentId;

    /** 预约单号 */
    private String appointmentNo;

    /** 当前状态 */
    private String currentStatus;

    /** 当前状态名称 */
    private String currentStatusName;

    /** 关联的订单ID（如果已创建订单） */
    private String orderId;

    /** 关联的订单号（如果已创建订单） */
    private String orderNo;

    /** 订单状态（如果已创建订单） */
    private Integer orderStatus;

    /** 订单状态名称（如果已创建订单） */
    private String orderStatusName;

    /** 时间轴节点列表 */
    private List<AppointmentTimelineNodeVO> nodes;
}
