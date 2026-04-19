package com.elderlycare.dto.elder;

import lombok.Data;
import java.time.LocalDate;

/**
 * 健康报告生成DTO
 */
@Data
public class HealthReportGenerateDTO {

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 报告类型：MONTHLY-月度报告，QUARTERLY-季度报告，YEARLY-年度报告，SPECIAL-专项报告
     */
    private String reportType;

    /**
     * 报告开始日期
     */
    private LocalDate startDate;

    /**
     * 报告结束日期
     */
    private LocalDate endDate;

    /**
     * 报告标题
     */
    private String title;
}
