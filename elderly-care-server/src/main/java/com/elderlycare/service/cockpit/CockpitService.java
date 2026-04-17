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
     */
    CockpitOverviewVO getOverview();

    /**
     * 获取订单趋势
     */
    List<OrderStatisticsVO.TrendData> getOrderTrend(String type);

    /**
     * 获取服务类型分布
     */
    List<CockpitOverviewVO.ServiceDistribution> getServiceDistribution();

    /**
     * 获取区域分布
     */
    List<CockpitOverviewVO.AreaDistribution> getAreaDistribution();

    /**
     * 获取服务商排行
     */
    List<CockpitOverviewVO.ProviderRanking> getProviderRanking(String type, Integer limit);

    /**
     * 获取服务人员排行
     */
    List<CockpitOverviewVO.StaffRanking> getStaffRanking(String type, Integer limit);

    /**
     * 获取满意度分布
     */
    Map<String, Long> getSatisfactionDistribution();

    /**
     * 获取质检分布
     */
    Map<String, Long> getQualityDistribution();

    /**
     * 获取财务趋势
     */
    List<FinancialStatisticsVO.MonthlyTrend> getFinancialTrend(String type);

    /**
     * 获取年龄段分布
     */
    List<ElderStatisticsVO.AgeDistribution> getAgeDistribution();

    /**
     * 获取护理等级分布
     */
    List<ElderStatisticsVO.CareLevelDistribution> getCareLevelDistribution();

    /**
     * 获取热力图数据（基于预约地址）
     */
    Map<String, Object> getHeatMapData();

    /**
     * 获取实时订单
     */
    List<Map<String, Object>> getRealtimeOrders(Integer limit);

    /**
     * 获取预警信息
     */
    List<Map<String, Object>> getWarnings(String level, Integer limit);
}
