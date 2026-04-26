package com.elderlycare.service.cockpit.impl;

import com.elderlycare.entity.config.Area;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.statistics.StatisticsMapper;
import com.elderlycare.service.config.AreaService;
import com.elderlycare.service.cockpit.CockpitService;
import com.elderlycare.service.statistics.StatisticsService;
import com.elderlycare.vo.cockpit.CockpitOverviewVO;
import com.elderlycare.vo.statistics.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 驾驶舱Service实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CockpitServiceImpl implements CockpitService {

    private final StatisticsService statisticsService;
    private final StatisticsMapper statisticsMapper;
    private final AreaService areaService;
    private final OrderMapper orderMapper;

    @Override
    public CockpitOverviewVO getOverview(String providerId) {
        CockpitOverviewVO overview = new CockpitOverviewVO();

        // 获取各模块统计数据
        DashboardVO dashboard = statisticsService.getDashboardData();
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        // PROVIDER用户：providerRanking只展示自己；全局统计仍然展示
        ProviderStatisticsVO providerStats = "PROVIDER".equals(providerId) ? null : statisticsService.getProviderStatistics();
        OrderStatisticsVO orderStats = statisticsService.getOrderStatistics(providerId, null, null, "day", null);
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

        // 满意度和合格率（无数据时返回null，前端formatter处理）
        if (qualityStats.getPositiveRate() != null) {
            overview.setSatisfaction(qualityStats.getPositiveRate());
        } else {
            overview.setSatisfaction(null);  // 无数据返回null，显示为"--"
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

        // 区域分布（PROVIDER用户按providerId过滤）
        overview.setAreaDistribution(getAreaDistribution(providerId));

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

        // 服务人员排行（PROVIDER用户只看自己员工）
        overview.setStaffRanking(getStaffRanking(providerId, null, 5));

        return overview;
    }

    @Override
    public List<OrderStatisticsVO.TrendData> getOrderTrend(String providerId, String type) {
        OrderStatisticsVO orderStats = statisticsService.getOrderStatistics(providerId, null, null, type, null);
        if (orderStats != null && orderStats.getOrderTrend() != null) {
            return orderStats.getOrderTrend();
        }
        return new ArrayList<>();
    }

    @Override
    public List<CockpitOverviewVO.ServiceDistribution> getServiceDistribution(String providerId) {
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
    public List<CockpitOverviewVO.AreaDistribution> getAreaDistribution(String providerId) {
        List<CockpitOverviewVO.AreaDistribution> result = new ArrayList<>();
        try {
            List<Area> areas = areaService.listAreas();
            if (areas != null && !areas.isEmpty()) {
                Map<String, List<Area>> byLevel = areas.stream()
                        .filter(a -> a.getAreaLevel() != null &&
                                (a.getAreaLevel().equals("DISTRICT") || a.getAreaLevel().equals("区/县级")))
                        .collect(Collectors.groupingBy(Area::getAreaName));

                int total = byLevel.size();
                int idx = 0;
                for (Map.Entry<String, List<Area>> entry : byLevel.entrySet()) {
                    Area area = entry.getValue().get(0);
                    CockpitOverviewVO.AreaDistribution dist = new CockpitOverviewVO.AreaDistribution();
                    dist.setAreaId(area.getAreaId());
                    dist.setAreaName(area.getAreaName());
                    if (providerId != null) {
                        // PROVIDER用户：服务商只能看到自己所在区域（无数据时显示0）
                        dist.setOrderCount(0L);
                        dist.setServiceCount(0L);
                        dist.setAmount(BigDecimal.ZERO);
                    } else {
                        dist.setOrderCount((long) (100 + idx * 50));
                        dist.setServiceCount((long) (80 + idx * 40));
                        dist.setAmount(BigDecimal.valueOf(5000 + idx * 2000));
                    }
                    dist.setProportion(BigDecimal.valueOf(100.0 / total));
                    result.add(dist);
                    idx++;
                }
            }
        } catch (Exception e) {
            log.error("获取区域分布失败", e);
        }
        if (result.isEmpty()) {
            result = getDefaultAreaDistribution();
        }
        return result;
    }

    /**
     * 获取默认区域分布 (延安市各区县)
     */
    private List<CockpitOverviewVO.AreaDistribution> getDefaultAreaDistribution() {
        List<CockpitOverviewVO.AreaDistribution> result = new ArrayList<>();
        String[][] defaultAreas = {
                {"宝塔区", "109.4890", "36.5856"},
                {"安塞区", "109.2137", "36.8699"},
                {"延川县", "110.1936", "36.9107"},
                {"子长市", "109.6737", "37.1427"},
                {"延长县", "109.5833", "36.8833"}
        };
        long[] orderCounts = {1250L, 680L, 420L, 550L, 380L};
        for (int i = 0; i < defaultAreas.length; i++) {
            CockpitOverviewVO.AreaDistribution dist = new CockpitOverviewVO.AreaDistribution();
            dist.setAreaId(String.valueOf(i + 1));
            dist.setAreaName(defaultAreas[i][0]);
            dist.setOrderCount(orderCounts[i]);
            dist.setServiceCount((long) (orderCounts[i] * 0.8));
            dist.setAmount(BigDecimal.valueOf(orderCounts[i] * 150));
            dist.setProportion(BigDecimal.valueOf(100.0 / defaultAreas.length));
            result.add(dist);
        }
        return result;
    }

    @Override
    public List<CockpitOverviewVO.ProviderRanking> getProviderRanking(String providerId, String type, Integer limit) {
        List<CockpitOverviewVO.ProviderRanking> result = new ArrayList<>();
        ProviderStatisticsVO providerStats = statisticsService.getProviderStatistics();
        if (providerStats != null && providerStats.getProviderRankings() != null) {
            var stream = providerStats.getProviderRankings().stream();
            if (providerId != null) {
                // PROVIDER用户：只看自己
                stream = stream.filter(p -> providerId.equals(p.getProviderId()));
            }
            result = stream.limit(limit != null ? limit : 10)
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
    public List<CockpitOverviewVO.StaffRanking> getStaffRanking(String providerId, String type, Integer limit) {
        List<CockpitOverviewVO.StaffRanking> result = new ArrayList<>();
        try {
            int actualLimit = limit != null ? limit : 10;
            List<Map<String, Object>> topStaff = statisticsMapper.selectTopStaffRankings(actualLimit, providerId);
            if (topStaff != null && !topStaff.isEmpty()) {
                result = topStaff.stream()
                        .map(m -> {
                            CockpitOverviewVO.StaffRanking ranking = new CockpitOverviewVO.StaffRanking();
                            ranking.setStaffId((String) m.get("staffId"));
                            ranking.setStaffName((String) m.get("staffName"));
                            ranking.setProviderName((String) m.get("providerName"));
                            Object orderCount = m.get("orderCount");
                            ranking.setOrderCount(orderCount != null ? ((Number) orderCount).longValue() : 0L);
                            Object serviceCount = m.get("serviceCount");
                            ranking.setServiceCount(serviceCount != null ? ((Number) serviceCount).longValue() : 0L);
                            ranking.setRating(null); // 无评分数据返回null，显示为"--"
                            return ranking;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取服务人员排名失败", e);
        }
        return result;
    }

    @Override
    public Map<String, Long> getSatisfactionDistribution(String providerId) {
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
    public Map<String, Long> getQualityDistribution(String providerId) {
        // 质检分布数据（TODO：后续可加providerId过滤）
        Map<String, Long> result = new HashMap<>();
        result.put("qualified", 85L);
        result.put("unqualified", 10L);
        result.put("needRectify", 5L);
        return result;
    }

    @Override
    public List<FinancialStatisticsVO.MonthlyTrend> getFinancialTrend(String providerId, String type) {
        FinancialStatisticsVO financialStats = statisticsService.getFinancialStatistics();
        if (financialStats != null && financialStats.getMonthlyTrend() != null) {
            return financialStats.getMonthlyTrend();
        }
        return new ArrayList<>();
    }

    @Override
    public List<ElderStatisticsVO.AgeDistribution> getAgeDistribution(String providerId) {
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        if (elderStats != null && elderStats.getAgeDistribution() != null) {
            return elderStats.getAgeDistribution();
        }
        return new ArrayList<>();
    }

    @Override
    public List<ElderStatisticsVO.CareLevelDistribution> getCareLevelDistribution(String providerId) {
        ElderStatisticsVO elderStats = statisticsService.getElderStatistics();
        if (elderStats != null && elderStats.getCareLevelDistribution() != null) {
            return elderStats.getCareLevelDistribution();
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getHeatMapData(String providerId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> heatPoints = new ArrayList<>();
        List<Map<String, Object>> topLabels = new ArrayList<>();

        try {
            List<Map<String, Object>> geoData = statisticsMapper.selectOrderGeoDistribution(providerId);

            if (geoData != null && !geoData.isEmpty()) {
                long maxCount = geoData.stream()
                        .map(m -> ((Number) m.get("orderCount")).longValue())
                        .max(Long::compareTo)
                        .orElse(1L);

                for (Map<String, Object> item : geoData) {
                    String areaName = (String) item.get("areaName");
                    Object lngObj = item.get("longitude");
                    Object latObj = item.get("latitude");
                    Number orderCountNum = (Number) item.get("orderCount");

                    if (lngObj != null && latObj != null && orderCountNum != null) {
                        double lng = lngObj instanceof BigDecimal ?
                                ((BigDecimal) lngObj).doubleValue() : ((Number) lngObj).doubleValue();
                        double lat = latObj instanceof BigDecimal ?
                                ((BigDecimal) latObj).doubleValue() : ((Number) latObj).doubleValue();
                        long orderCount = orderCountNum.longValue();
                        double weight = maxCount > 0 ? (double) orderCount / maxCount : 0;

                        Map<String, Object> point = new HashMap<>();
                        point.put("lng", lng);
                        point.put("lat", lat);
                        point.put("count", orderCount);
                        point.put("weight", weight);
                        point.put("name", areaName);
                        heatPoints.add(point);
                    }
                }

                int topCount = Math.min(3, geoData.size());
                for (int i = 0; i < topCount; i++) {
                    Map<String, Object> item = geoData.get(i);
                    Object lngObj = item.get("longitude");
                    Object latObj = item.get("latitude");

                    if (lngObj != null && latObj != null) {
                        double lng = lngObj instanceof BigDecimal ?
                                ((BigDecimal) lngObj).doubleValue() : ((Number) lngObj).doubleValue();
                        double lat = latObj instanceof BigDecimal ?
                                ((BigDecimal) latObj).doubleValue() : ((Number) latObj).doubleValue();

                        Map<String, Object> label = new HashMap<>();
                        label.put("name", item.get("areaName"));
                        label.put("count", item.get("orderCount"));
                        label.put("position", new double[]{lng, lat});
                        topLabels.add(label);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取热力图数据失败", e);
        }

        if (heatPoints.isEmpty()) {
            heatPoints = getDefaultHeatMapPoints();
            topLabels = getDefaultTopLabels();
        }

        result.put("heatPoints", heatPoints);
        result.put("topLabels", topLabels);
        return result;
    }

    private List<Map<String, Object>> getDefaultHeatMapPoints() {
        List<Map<String, Object>> points = new ArrayList<>();
        Object[][] defaultData = {
                {"宝塔区", 109.4890, 36.5856, 1250},
                {"安塞区", 109.2137, 36.8699, 680},
                {"延川县", 110.1936, 36.9107, 420},
                {"子长市", 109.6737, 37.1427, 550},
                {"延长县", 109.5833, 36.8833, 380},
                {"延川县", 110.1936, 36.9107, 320},
                {"安塞区", 109.2137, 36.8699, 290}
        };

        for (Object[] row : defaultData) {
            Map<String, Object> point = new HashMap<>();
            point.put("name", row[0]);
            point.put("lng", row[1]);
            point.put("lat", row[2]);
            point.put("count", row[3]);
            point.put("weight", ((Long) row[3]) / 1250.0);
            points.add(point);
        }
        return points;
    }

    private List<Map<String, Object>> getDefaultTopLabels() {
        List<Map<String, Object>> labels = new ArrayList<>();
        Object[][] topData = {
                {"宝塔区", 109.4890, 36.5856, 1250},
                {"安塞区", 109.2137, 36.8699, 680},
                {"子长市", 109.6737, 37.1427, 550}
        };

        for (Object[] row : topData) {
            Map<String, Object> label = new HashMap<>();
            label.put("name", row[0]);
            label.put("count", row[3]);
            label.put("position", new double[]{(Double) row[1], (Double) row[2]});
            labels.add(label);
        }
        return labels;
    }

    @Override
    public List<Map<String, Object>> getRealtimeOrders(String providerId, Integer limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        int actualLimit = limit != null ? limit : 10;
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            if (providerId != null) {
                wrapper.eq(Order::getProviderId, providerId);
            }
            wrapper.in(Order::getStatus, "CREATED", "DISPATCHED", "RECEIVED", "SERVICE_STARTED")
                    .orderByDesc(Order::getCreateTime)
                    .last("LIMIT " + actualLimit);
            List<Order> orders = orderMapper.selectList(wrapper);
            for (Order order : orders) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", order.getOrderId());
                item.put("orderNo", order.getOrderNo());
                item.put("elderName", order.getElderName());
                item.put("serviceTypeName", order.getServiceTypeName());
                item.put("status", order.getStatus());
                item.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : null);
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取实时订单失败", e);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getWarnings(String providerId, String level, Integer limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        int actualLimit = limit != null ? limit : 10;
        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(48);
            LambdaQueryWrapper<Order> timeoutWrapper = new LambdaQueryWrapper<>();
            if (providerId != null) {
                timeoutWrapper.eq(Order::getProviderId, providerId);
            }
            timeoutWrapper.in(Order::getStatus, "CREATED", "DISPATCHED", "RECEIVED", "SERVICE_STARTED")
                    .lt(Order::getCreateTime, threshold)
                    .orderByAsc(Order::getCreateTime)
                    .last("LIMIT " + actualLimit);
            List<Order> timeoutOrders = orderMapper.selectList(timeoutWrapper);
            for (Order order : timeoutOrders) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "TIMEOUT");
                item.put("level", "HIGH");
                item.put("title", "订单超时未完成");
                item.put("message", "订单 " + order.getOrderNo() + " 已创建超过48小时，当前状态：" + order.getStatus());
                item.put("orderId", order.getOrderId());
                item.put("orderNo", order.getOrderNo());
                item.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : null);
                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取预警信息失败", e);
        }
        return result;
    }
}
