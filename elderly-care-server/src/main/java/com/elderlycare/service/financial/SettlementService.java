package com.elderlycare.service.financial;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.BatchSettlementDTO;
import com.elderlycare.dto.financial.SettlementCalculateDTO;
import com.elderlycare.dto.financial.SettlementQueryDTO;
import com.elderlycare.vo.financial.SettlementCalculateVO;
import com.elderlycare.vo.financial.SettlementVO;

import java.util.List;

/**
 * 结算Service接口
 */
public interface SettlementService {

    /**
     * 计算结算费用
     */
    SettlementCalculateVO calculateSettlement(SettlementCalculateDTO dto);

    /**
     * 确认结算
     */
    void confirmSettlement(String settlementId);

    /**
     * 查询结算列表
     */
    PageResult<SettlementVO> querySettlements(SettlementQueryDTO dto);

    /**
     * 获取结算详情
     */
    SettlementVO getSettlementById(String settlementId);

    /**
     * 批量结算
     */
    List<String> batchSettlement(BatchSettlementDTO dto);
}
