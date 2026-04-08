package com.elderlycare.service.financial;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.RefundAuditDTO;
import com.elderlycare.dto.financial.RefundCreateDTO;
import com.elderlycare.dto.financial.RefundQueryDTO;
import com.elderlycare.vo.financial.RefundVO;

/**
 * 退款Service接口
 */
public interface RefundService {

    /**
     * 申请退款
     */
    String createRefund(RefundCreateDTO dto);

    /**
     * 查询退款列表
     */
    PageResult<RefundVO> queryRefunds(RefundQueryDTO dto);

    /**
     * 获取退款详情
     */
    RefundVO getRefundById(String refundId);

    /**
     * 审核退款
     */
    void auditRefund(String refundId, RefundAuditDTO dto);
}
