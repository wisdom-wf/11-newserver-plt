package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 时间轴节点VO
 */
@Data
public class TimelineNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 节点标题 */
    private String title;

    /** 时间戳 */
    private String time;

    /** 详情列表 */
    private List<DetailItemVO> details;

    /** 是否已完成 */
    private Boolean completed;

    /** 是否为当前活动节点 */
    private Boolean active;

    /**
     * 详情项VO
     */
    @Data
    public static class DetailItemVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String label;
        private String value;
    }
}
