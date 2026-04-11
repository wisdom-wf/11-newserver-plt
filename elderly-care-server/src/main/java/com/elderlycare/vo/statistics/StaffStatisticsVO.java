package com.elderlycare.vo.statistics;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务人员统计VO
 */
@Data
public class StaffStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总数 */
    private Long total;

    /** 在职 */
    private Long active;

    /** 待上岗 */
    private Long pending;

    /** 已离职 */
    private Long inactive;
}
