package com.elderlycare.service.quality;

import com.elderlycare.common.PageResult;
import com.elderlycare.entity.quality.QualityAlert;

/**
 * 质量预警服务接口
 */
public interface QualityAlertService {

    /**
     * 创建预警
     */
    String createAlert(QualityAlert alert);

    /**
     * 获取预警列表
     */
    PageResult<QualityAlert> getAlertList(String providerId, String staffId, String alertType, String alertStatus, int page, int pageSize);

    /**
     * 获取预警详情
     */
    QualityAlert getAlertById(String alertId);

    /**
     * 处理预警
     */
    void handleAlert(String alertId, String handleResult);

    /**
     * 忽略预警
     */
    void ignoreAlert(String alertId);

    /**
     * 检查并生成预警（评分过低、投诉增多等）
     */
    void checkAndGenerateAlerts();
}
