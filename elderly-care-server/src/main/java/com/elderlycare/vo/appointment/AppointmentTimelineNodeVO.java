package com.elderlycare.vo.appointment;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 预约时间轴节点VO
 */
@Data
public class AppointmentTimelineNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private String status;

    /** 状态名称 */
    private String statusName;

    /** 节点标题 */
    private String title;

    /** 时间戳 */
    private String time;

    /** 详情列表 */
    private List<AppointmentTimelineDetailVO> details;

    /** 是否已完成 */
    private Boolean completed;

    /** 是否为当前活动节点 */
    private Boolean active;

    /**
     * 详情项VO
     */
    @Data
    public static class AppointmentTimelineDetailVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String label;
        private String value;
    }
}
