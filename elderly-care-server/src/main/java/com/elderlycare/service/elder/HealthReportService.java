package com.elderlycare.service.elder;

import com.elderlycare.dto.elder.HealthReportGenerateDTO;
import com.elderlycare.entity.elder.HealthReport;
import com.elderlycare.vo.elder.HealthReportVO;

import java.util.List;

/**
 * 健康报告Service接口
 */
public interface HealthReportService {

    /**
     * 生成健康报告
     */
    HealthReport generateReport(HealthReportGenerateDTO dto);

    /**
     * 获取老人健康报告列表
     */
    List<HealthReportVO> getReportList(String elderId);

    /**
     * 获取报告详情
     */
    HealthReportVO getReportById(String reportId);

    /**
     * 下载报告PDF
     */
    byte[] downloadPdf(String reportId);

    /**
     * 删除报告
     */
    void deleteReport(String reportId);
}
