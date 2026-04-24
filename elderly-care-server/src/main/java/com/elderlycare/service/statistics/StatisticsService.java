package com.elderlycare.service.statistics;

import com.elderlycare.vo.statistics.*;

/**
 * 数据统计Service接口
 */
public interface StatisticsService {

    /**
     * 获取仪表盘数据
     */
    DashboardVO getDashboardData();

    /**
     * 获取老人统计数据
     */
    ElderStatisticsVO getElderStatistics();

    /**
     * 获取服务商统计数据
     */
    ProviderStatisticsVO getProviderStatistics();

    /**
     * 获取订单统计数据
     *
     * @param providerId      服务商ID，null表示全平台（仅SYSTEM/ADMIN可见）
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @param groupBy        分组维度：day, week, month
     * @param serviceTypeCode 服务类型编码
     */
    OrderStatisticsVO getOrderStatistics(String providerId, String startDate, String endDate, String groupBy, String serviceTypeCode);

    /**
     * 获取财务统计数据
     */
    FinancialStatisticsVO getFinancialStatistics();

    /**
     * 获取质量统计数据
     */
    QualityStatisticsVO getQualityStatistics();

    /**
     * 获取服务人员统计数据
     */
    StaffStatisticsVO getStaffStatistics();
}
