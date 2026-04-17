package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 订单时间轴详情VO（包含完整的节点信息）
 */
@Data
public class OrderTimelineDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private String orderId;

    /** 订单号 */
    private String orderNo;

    /** 当前状态 */
    private Integer currentStatus;

    /** 当前状态名称 */
    private String currentStatusName;

    /** 时间轴节点列表 */
    private List<TimelineNodeVO> nodes;
}
