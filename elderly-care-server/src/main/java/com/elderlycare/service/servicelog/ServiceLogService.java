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
    void submitServiceLog(ServiceLogVO vo);

    /**
     * 更新服务日志
     */
    void updateServiceLog(String id, ServiceLogVO vo);

    /**
     * 上报异常
     */
    void reportAnomaly(String id, Map<String, Object> params);

    /**
     * 提交审核
     */
    void submitForReview(String id, String reviewRemarks);

    /**
     * 删除服务日志
     */
    void deleteServiceLog(String id);

    /**
     * 获取服务日志统计
     */
    ServiceLogStatisticsVO getStatistics(String areaId, String providerId, String staffId, String startDate, String endDate);
}
