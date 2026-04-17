package com.elderlycare.dto.evaluation;

import lombok.Data;
import java.io.Serializable;

/**
 * 评价查询DTO
 */
@Data
public class EvaluationQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 订单号（文本搜索）
     */
    private String orderNo;

    /**
     * 老人姓名（文本搜索）
     */
    private String elderName;

    /**
     * 服务商名称（文本搜索）
     */
    private String providerName;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 服务类型编码
     */
    private String serviceTypeCode;

    /**
     * 评分范围-最小值
     */
    private Integer minRating;

    /**
     * 评分范围-最大值
     */
    private Integer maxRating;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
