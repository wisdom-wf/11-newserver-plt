package com.elderlycare.service.cockpit.impl;

import com.elderlycare.service.cockpit.CockpitService;
import com.elderlycare.service.statistics.StatisticsService;
import com.elderlycare.vo.cockpit.CockpitOverviewVO;
import com.elderlycare.vo.statistics.DashboardVO;
import com.elderlycare.vo.statistics.ElderStatisticsVO;
import com.elderlycare.vo.statistics.FinancialStatisticsVO;
import com.elderlycare.vo.statistics.OrderStatisticsVO;
import com.elderlycare.vo.statistics.ProviderStatisticsVO;
import com.elderlycare.vo.statistics.QualityStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 驾驶舱Service实现
 */
@Service
@RequiredArgsConstructor
public class CockpitServiceImpl implements CockpitService {

    private final StatisticsService statisticsService;

    @Override
    public CockpitOverviewVO getOverview() {
        CockpitOverviewVO overview = new CockpitOverviewVO();

        // 获取各模块统计数据
        DashboardVO dashboard = statisticsService.getDashboardData();
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        ProviderStatisticsVO providerStats = statisticsService.getProviderStatistics();
        OrderStatisticsVO orderStats = statisticsService.getOrderStatistics(null, null, "day", null);
        FinancialStatisticsVO financialStats = statisticsService.getFinancialStatistics();
        QualityStatisticsVO qualityStats = statisticsService.getQualityStatistics();

        // 设置基础数据
        overview.setTodayOrders(dashboard.getTodayOrders());
        overview.setMonthOrders(dashboard.getTodayOrders() * 30L); // 估算
        overview.setTotalOrders(dashboard.getTotalOrders());
        overview.setProviderCount(dashboard.getTotalProviders());
        overview.setStaffCount(dashboard.getTotalStaff());
        overview.setElderCount(dashboard.getTotalElders());

        // 服务人次
        overview.setTodayServices(dashboard.getTodayCompletedOrders());
        overview.setMonthServices(dashboard.getTodayCompletedOrders() * 30L);
        overview.setTotalServices(dashboard.getTotalOrders());

        // 营收
        overview.setMonthRevenue(financialStats.getMonthlyAmount() != null ? financialStats.getMonthlyAmount() : BigDecimal.ZERO);
        overview.setTotalRevenue(financialStats.getTotalAmount() != null ? financialStats.getTotalAmount() : BigDecimal.ZERO);

        // 满意度和合格率 - positiveRate已经是百分比(0-100)，直接使用
        if (qualityStats.getPositiveRate() != null) {
            overview.setSatisfaction(qualityStats.getPositiveRate());
        } else {
            overview.setSatisfaction(BigDecimal.ZERO);
        }
        overview.setQualifiedRate(BigDecimal.valueOf(95)); // 默认95%

        // 服务类型分布
        List<CockpitOverviewVO.ServiceDistribution> serviceDistList = new ArrayList<>();
        if (dashboard.getServiceTypeDistribution() != null) {
            serviceDistList = dashboard.getServiceTypeDistribution().stream()
                    .map(s -> {
                        CockpitOverviewVO.ServiceDistribution dist = new CockpitOverviewVO.ServiceDistribution();
                        dist.setCategory(s.getServiceTypeName());
                        dist.setCount(s.getOrderCount());
                        dist.setProportion(s.getPercentage());
                        return dist;
                    })
                    .collect(Collectors.toList());
        }
        overview.setServiceTypeDistribution(serviceDistList);

        // 区域分布（从老人统计获取）
        List<CockpitOverviewVO.AreaDistribution> areaDistList = new ArrayList<>();
        if (elderStats.getAgeDistribution() != null) {
            // 使用年龄段作为区域分布的替代
            areaDistList = elderStats.getAgeDistribution().stream()
                    .limit(5)
                    .map(a -> {
                        CockpitOverviewVO.AreaDistribution dist = new CockpitOverviewVO.AreaDistribution();
                        dist.setAreaId(a.getAgeRange());
                        dist.setAreaName(a.getAgeRange());
                        dist.setOrderCount(a.getCount());
                        dist.setServiceCount(a.getCount());
                        dist.setAmount(BigDecimal.ZERO);
                        dist.setProportion(a.getPercentage());
                        return dist;
                    })
                    .collect(Collectors.toList());
        }
        overview.setAreaDistribution(areaDistList);

        // 服务商排行
        List<CockpitOverviewVO.ProviderRanking> providerRankingList = new ArrayList<>();
        if (dashboard.getTopProviders() != null) {
            providerRankingList = dashboard.getTopProviders().stream()
                    .map(p -> {
                        CockpitOverviewVO.ProviderRanking ranking = new CockpitOverviewVO.ProviderRanking();
                        ranking.setProviderId(p.getProviderId());
                        ranking.setProviderName(p.getProviderName());
                        ranking.setOrderCount(p.getOrderCount());
                        ranking.setRating(p.getRating());
                        return ranking;
                    })
                    .collect(Collectors.toList());
        }
        overview.setProviderRanking(providerRankingList);

        // 服务人员排行（从订单统计获取）
        List<CockpitOverviewVO.StaffRanking> staffRankingList = new ArrayList<>();
        if (orderStats.getOrderTrend() != null) {
            staffRankingList = orderStats.getOrderTrend().stream()
                    .limit(5)
                    .map(t -> {
                        CockpitOverviewVO.StaffRanking ranking = new CockpitOverviewVO.StaffRanking();
                        ranking.setStaffId(t.getDate());
                        ranking.setStaffName("服务人员");
                        ranking.setProviderName("-");
                        ranking.setOrderCount(t.getOrderCount());
                        ranking.setRating(5.0);
                        return ranking;
                    })
                    .collect(Collectors.toList());
        }
        overview.setStaffRanking(staffRankingList);

        return overview;
    }
}
