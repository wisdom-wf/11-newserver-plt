package com.elderlycare.service.financial;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.FinancialReportQueryDTO;
import com.elderlycare.vo.financial.FinancialReportVO;

/**
 * 财务报表Service接口
 */
public interface FinancialReportService {

    /**
     * 查询财务报表
     */
    PageResult<FinancialReportVO> queryReports(FinancialReportQueryDTO dto);
}
