package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康报告实体
 */
@Data
@TableName("t_health_report")
public class HealthReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String reportId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 报告编号
     */
    private String reportNo;

    /**
     * 报告日期
     */
    private LocalDate reportDate;

    /**
     * 报告类型：MONTHLY-月度报告，QUARTERLY-季度报告，YEARLY-年度报告，SPECIAL-专项报告
     */
    private String reportType;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 报告内容（JSON格式）
     */
    private String content;

    /**
     * PDF存储路径
     */
    private String pdfUrl;

    /**
     * 生成的员工ID
     */
    private String staffId;

    /**
     * 生成员工姓名
     */
    private String staffName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
