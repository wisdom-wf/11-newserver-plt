package com.elderlycare.service.servicelog;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.servicelog.ServiceLogQueryDTO;
import com.elderlycare.vo.servicelog.ServiceLogStatisticsVO;
import com.elderlycare.vo.servicelog.ServiceLogVO;

import java.util.Map;

/**
 * 服务日志服务接口
 */
public interface ServiceLogService {

    /**
     * 获取服务日志列表
     */
    PageResult<ServiceLogVO> getServiceLogList(ServiceLogQueryDTO query);

    /**
     * 获取服务日志详情
     */
    ServiceLogVO getServiceLog(String id);

    /**
     * 根据订单ID获取服务日志
     */
    ServiceLogVO getServiceLogByOrderId(String orderId);

    /**
     * 提交服务日志
     */
    String submitServiceLog(ServiceLogVO vo);

    /**
     * 更新服务日志
     */
    void updateServiceLog(String id, ServiceLogVO vo);

    /**
     * 上报异常
     */
    void reportAnomaly(String id, Map<String, Object> params);

    /**
     * 获取服务日志统计
     */
    ServiceLogStatisticsVO getStatistics(String areaId, String providerId, String staffId, String startDate, String endDate);

    /**
     * 提交服务日志审核
     */
    void submitForReview(String id);

    /**
     * 审核服务日志
     * @param id 服务日志ID
     * @param result 审核结果 APPROVED/REJECTED
     * @param reviewComment 审核意见
     */
    void reviewServiceLog(String id, String result, String reviewComment);

    /**
     * 删除服务日志
     */
    void deleteServiceLog(String id);
}
