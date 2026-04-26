package com.elderlycare.service.cockpit;

import com.elderlycare.vo.cockpit.CockpitOverviewVO;
import com.elderlycare.vo.statistics.OrderStatisticsVO;
import com.elderlycare.vo.statistics.ProviderStatisticsVO;
import com.elderlycare.vo.statistics.QualityStatisticsVO;
import com.elderlycare.vo.statistics.FinancialStatisticsVO;
import com.elderlycare.vo.statistics.ElderStatisticsVO;

import java.util.List;
import java.util.Map;

/**
 * 驾驶舱Service接口
 */
public interface CockpitService {

    /**
     * 获取驾驶舱概览数据
     * @param providerId PROVIDER用户强制注入，用于过滤统计数据；ADMIN/CITY_ADMIN传入null表示全局
     */
    CockpitOverviewVO getOverview(String providerId);

    /**
     * 获取订单趋势
     */
    List<OrderStatisticsVO.TrendData> getOrderTrend(String providerId, String type);

    /**
     * 获取服务类型分布
     */
    List<CockpitOverviewVO.ServiceDistribution> getServiceDistribution(String providerId);

    /**
     * 获取区域分布（PROVIDER用户过滤为自己所在区域）
     */
    List<CockpitOverviewVO.AreaDistribution> getAreaDistribution(String providerId);

    /**
     * 获取服务商排行（PROVIDER用户只看自己）
     */
    List<CockpitOverviewVO.ProviderRanking> getProviderRanking(String providerId, String type, Integer limit);

    /**
     * 获取服务人员排行（PROVIDER用户只看自己员工）
     */
    List<CockpitOverviewVO.StaffRanking> getStaffRanking(String providerId, String type, Integer limit);

    /**
     * 获取满意度分布（PROVIDER用户只看自己评价）
     */
    Map<String, Long> getSatisfactionDistribution(String providerId);

    /**
     * 获取质检分布（PROVIDER用户只看自己质检）
     */
    Map<String, Long> getQualityDistribution(String providerId);

    /**
     * 获取财务趋势（PROVIDER用户只看自己财务）
     */
    List<FinancialStatisticsVO.MonthlyTrend> getFinancialTrend(String providerId, String type);

    /**
     * 获取年龄段分布
     */
    List<ElderStatisticsVO.AgeDistribution> getAgeDistribution(String providerId);

    /**
     * 获取护理等级分布
     */
    List<ElderStatisticsVO.CareLevelDistribution> getCareLevelDistribution(String providerId);

    /**
     * 获取热力图数据（基于预约地址，PROVIDER用户只看自己订单）
     */
    Map<String, Object> getHeatMapData(String providerId);

    /**
     * 获取实时订单（PROVIDER用户只看自己订单）
     */
    List<Map<String, Object>> getRealtimeOrders(String providerId, Integer limit);

    /**
     * 获取预警信息（PROVIDER用户只看自己预警）
     */
    List<Map<String, Object>> getWarnings(String providerId, String level, Integer limit);
}
