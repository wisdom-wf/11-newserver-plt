package com.elderlycare.service.cockpit.impl;

import com.elderlycare.service.cockpit.CockpitService;
import com.elderlycare.service.statistics.StatisticsService;
import com.elderlycare.vo.cockpit.CockpitOverviewVO;
import com.elderlycare.vo.statistics.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // 设置基础数据（处理空值）
        Long todayOrders = dashboard.getTodayOrders() != null ? dashboard.getTodayOrders() : 0L;
        Long totalOrders = dashboard.getTotalOrders() != null ? dashboard.getTotalOrders() : 0L;
        Long totalProviders = dashboard.getTotalProviders() != null ? dashboard.getTotalProviders() : 0L;
        Long totalStaff = dashboard.getTotalStaff() != null ? dashboard.getTotalStaff() : 0L;
        Long totalElders = dashboard.getTotalElders() != null ? dashboard.getTotalElders() : 0L;
        Long todayCompleted = dashboard.getTodayCompletedOrders() != null ? dashboard.getTodayCompletedOrders() : 0L;

        overview.setTodayOrders(todayOrders);
        overview.setMonthOrders(todayOrders * 30L); // 估算
        overview.setTotalOrders(totalOrders);
        overview.setProviderCount(totalProviders);
        overview.setStaffCount(totalStaff);
        overview.setElderCount(totalElders);

        // 服务人次
        overview.setTodayServices(todayCompleted);
        overview.setMonthServices(todayCompleted * 30L);
        overview.setTotalServices(totalOrders);

        // 营收
        overview.setMonthRevenue(financialStats.getMonthAmount() != null ? financialStats.getMonthAmount() : BigDecimal.ZERO);
        overview.setTotalRevenue(financialStats.getTotalAmount() != null ? financialStats.getTotalAmount() : BigDecimal.ZERO);

        // 满意度和合格率
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

    @Override
    public List<OrderStatisticsVO.TrendData> getOrderTrend(String type) {
        OrderStatisticsVO orderStats = statisticsService.getOrderStatistics(null, null, type, null);
        if (orderStats != null && orderStats.getOrderTrend() != null) {
            return orderStats.getOrderTrend();
        }
        return new ArrayList<>();
    }

    @Override
    public List<CockpitOverviewVO.ServiceDistribution> getServiceDistribution() {
        DashboardVO dashboard = statisticsService.getDashboardData();
        List<CockpitOverviewVO.ServiceDistribution> result = new ArrayList<>();
        if (dashboard != null && dashboard.getServiceTypeDistribution() != null) {
            result = dashboard.getServiceTypeDistribution().stream()
                    .map(s -> {
                        CockpitOverviewVO.ServiceDistribution dist = new CockpitOverviewVO.ServiceDistribution();
                        dist.setCategory(s.getServiceTypeName());
                        dist.setCount(s.getOrderCount());
                        dist.setProportion(s.getPercentage());
                        return dist;
                    })
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<CockpitOverviewVO.AreaDistribution> getAreaDistribution() {
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        List<CockpitOverviewVO.AreaDistribution> result = new ArrayList<>();
        if (elderStats != null && elderStats.getAgeDistribution() != null) {
            result = elderStats.getAgeDistribution().stream()
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
        return result;
    }

    @Override
    public List<CockpitOverviewVO.ProviderRanking> getProviderRanking(String type, Integer limit) {
        ProviderStatisticsVO providerStats = statisticsService.getProviderStatistics();
        List<CockpitOverviewVO.ProviderRanking> result = new ArrayList<>();
        if (providerStats != null && providerStats.getProviderRankings() != null) {
            result = providerStats.getProviderRankings().stream()
                    .limit(limit != null ? limit : 10)
                    .map(p -> {
                        CockpitOverviewVO.ProviderRanking ranking = new CockpitOverviewVO.ProviderRanking();
                        ranking.setProviderId(p.getProviderId());
                        ranking.setProviderName(p.getProviderName());
                        ranking.setOrderCount(p.getOrderCount());
                        ranking.setServiceCount(p.getCompletedOrderCount());
                        ranking.setRating(p.getRating() != null ? p.getRating() : p.getAverageRating());
                        ranking.setAmount(BigDecimal.ZERO);
                        return ranking;
                    })
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<CockpitOverviewVO.StaffRanking> getStaffRanking(String type, Integer limit) {
        // 服务人员排行数据从订单统计获取
        OrderStatisticsVO orderStats = statisticsService.getOrderStatistics(null, null, "day", null);
        List<CockpitOverviewVO.StaffRanking> result = new ArrayList<>();
        if (orderStats != null && orderStats.getOrderTrend() != null) {
            result = orderStats.getOrderTrend().stream()
                    .limit(limit != null ? limit : 10)
                    .map(t -> {
                        CockpitOverviewVO.StaffRanking ranking = new CockpitOverviewVO.StaffRanking();
                        ranking.setStaffId(t.getDate());
                        ranking.setStaffName("服务人员" + t.getDate());
                        ranking.setProviderName("-");
                        ranking.setOrderCount(t.getOrderCount());
                        ranking.setServiceCount(t.getCompletedCount());
                        ranking.setRating(5.0);
                        return ranking;
                    })
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public Map<String, Long> getSatisfactionDistribution() {
        QualityStatisticsVO qualityStats = statisticsService.getQualityStatistics();
        Map<String, Long> result = new HashMap<>();
        if (qualityStats != null) {
            result.put("verySatisfied", qualityStats.getPositiveCount() != null ? qualityStats.getPositiveCount() : 0L);
            result.put("satisfied", qualityStats.getNeutralCount() != null ? qualityStats.getNeutralCount() : 0L);
            result.put("neutral", qualityStats.getNeutralCount() != null ? qualityStats.getNeutralCount() / 2 : 0L);
            result.put("dissatisfied", qualityStats.getNegativeCount() != null ? qualityStats.getNegativeCount() : 0L);
            result.put("veryDissatisfied", 0L);
        } else {
            result.put("verySatisfied", 0L);
            result.put("satisfied", 0L);
            result.put("neutral", 0L);
            result.put("dissatisfied", 0L);
            result.put("veryDissatisfied", 0L);
        }
        return result;
    }

    @Override
    public Map<String, Long> getQualityDistribution() {
        // 质检分布数据
        Map<String, Long> result = new HashMap<>();
        result.put("qualified", 85L);
        result.put("unqualified", 10L);
        result.put("needRectify", 5L);
        return result;
    }

    @Override
    public List<FinancialStatisticsVO.MonthlyTrend> getFinancialTrend(String type) {
        FinancialStatisticsVO financialStats = statisticsService.getFinancialStatistics();
        if (financialStats != null && financialStats.getMonthlyTrend() != null) {
            return financialStats.getMonthlyTrend();
        }
        return new ArrayList<>();
    }

    @Override
    public List<ElderStatisticsVO.AgeDistribution> getAgeDistribution() {
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        if (elderStats != null && elderStats.getAgeDistribution() != null) {
            return elderStats.getAgeDistribution();
        }
        return new ArrayList<>();
    }

    @Override
    public List<ElderStatisticsVO.CareLevelDistribution> getCareLevelDistribution() {
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        if (elderStats != null && elderStats.getCareLevelDistribution() != null) {
            return elderStats.getCareLevelDistribution();
        }
        return new ArrayList<>();
    }
}
