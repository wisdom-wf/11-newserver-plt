package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康报告VO
 */
@Data
public class HealthReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    private String reportId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 报告编号
     */
    private String reportNo;

    /**
     * 报告日期
     */
    private LocalDate reportDate;

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 报告类型名称
     */
    private String reportTypeName;

    /**
     * 报告标题
     */
    private String title;

    /**
     * PDF存储路径
     */
    private String pdfUrl;

    /**
     * 生成员工ID
     */
    private String staffId;

    /**
     * 生成员工姓名
     */
    private String staffName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
