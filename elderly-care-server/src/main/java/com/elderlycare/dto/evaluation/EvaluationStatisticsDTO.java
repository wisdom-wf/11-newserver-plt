package com.elderlycare.dto.evaluation;

import lombok.Data;
import java.io.Serializable;

/**
 * 评价统计DTO
 */
@Data
public class EvaluationStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型编码
     */
    private String serviceTypeCode;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
