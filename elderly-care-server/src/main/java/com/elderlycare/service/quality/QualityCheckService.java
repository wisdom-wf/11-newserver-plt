package com.elderlycare.service.quality;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.quality.QualityCheckQueryDTO;
import com.elderlycare.vo.quality.QualityCheckStatisticsVO;
import com.elderlycare.vo.quality.QualityCheckVO;

import java.util.Map;

/**
 * 质检服务接口
 */
public interface QualityCheckService {

    /**
     * 获取质检列表
     */
    PageResult<QualityCheckVO> getQualityCheckList(QualityCheckQueryDTO query);

    /**
     * 获取质检详情
     */
    QualityCheckVO getQualityCheck(String id);

    /**
     * 根据订单ID获取质检
     */
    QualityCheckVO getQualityCheckByOrderId(String orderId);

    /**
     * 创建质检
     */
    void createQualityCheck(QualityCheckVO vo);

    /**
     * 更新质检
     */
    void updateQualityCheck(String id, QualityCheckVO vo);

    /**
     * 提交整改
     */
    void submitRectify(String id, Map<String, Object> params);

    /**
     * 复检
     */
    void recheck(String id, Map<String, Object> params);

    /**
     * 执行质检（质检员提交质检结论，QUALIFIED/UNQUALIFIED/NEED_RECTIFY）
     * QUALIFIED → 联动日志/订单状态变为 COMPLETED
     * NEED_RECTIFY → 设置整改通知和期限
     * @param id 质检ID
     * @param dto 质检执行数据
     */
    void inspect(String id, com.elderlycare.dto.quality.InspectionDTO dto);

    /**
     * 获取质检统计
     */
    QualityCheckStatisticsVO getStatistics(String areaId, String providerId, String startDate, String endDate);
}
