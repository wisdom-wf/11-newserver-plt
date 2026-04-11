package com.elderlycare.service.statistics.impl;

import com.elderlycare.mapper.statistics.StatisticsMapper;
import com.elderlycare.service.statistics.StatisticsService;
import com.elderlycare.vo.statistics.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据统计Service实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        // 基本统计数据
        vo.setTotalElders(statisticsMapper.selectTotalElders());
        vo.setTotalProviders(statisticsMapper.selectTotalProviders());
        vo.setTotalStaff(statisticsMapper.selectTotalStaff());
        vo.setTotalOrders(statisticsMapper.selectTotalOrders());

        // 今日概况
        vo.setTodayOrders(statisticsMapper.selectTodayOrders());
        vo.setTodayCompletedOrders(statisticsMapper.selectTodayCompletedOrders());
        vo.setTodayPendingOrders(statisticsMapper.selectTodayPendingOrders());
        vo.setTodayCancelledOrders(statisticsMapper.selectTodayCancelledOrders());

        // 服务类型分布
        vo.setServiceTypeDistribution(convertServiceTypeDistribution(statisticsMapper.selectServiceTypeDistribution()));

        // 订单趋势
        vo.setOrderTrend(convertOrderTrend(statisticsMapper.selectOrderTrendLast7Days()));

        // TOP服务商
        vo.setTopProviders(convertTopProviders(statisticsMapper.selectTopProviders()));

        return vo;
    }

    @Override
    public ElderStatisticsVO getElderStatistics() {
        ElderStatisticsVO vo = new ElderStatisticsVO();

        vo.setTotalElders(statisticsMapper.selectTotalElders());
        vo.setMonthlyNewElders(statisticsMapper.selectMonthlyNewElders());
        vo.setActiveElders(statisticsMapper.selectActiveElders());

        // 年龄段分布
        vo.setAgeDistribution(convertElderAgeDistribution(statisticsMapper.selectElderAgeDistribution()));

        // 护理级别分布
        vo.setCareLevelDistribution(convertCareLevelDistribution(statisticsMapper.selectElderCareLevelDistribution()));

        // 服务需求分布
        vo.setServiceDemandDistribution(convertServiceDemandDistribution(statisticsMapper.selectElderServiceDemandDistribution()));

        return vo;
    }

    @Override
    public ProviderStatisticsVO getProviderStatistics() {
        ProviderStatisticsVO vo = new ProviderStatisticsVO();

        vo.setTotalProviders(statisticsMapper.selectTotalProviders());
        vo.setPendingProviders(statisticsMapper.selectPendingProviders());
        vo.setApprovedProviders(statisticsMapper.selectApprovedProviders());

        // 类型分布
        vo.setTypeDistribution(convertProviderTypeDistribution(statisticsMapper.selectProviderTypeDistribution()));

        // 服务商排名
        vo.setProviderRankings(convertProviderRankings(statisticsMapper.selectProviderRankings()));

        return vo;
    }

    @Override
    public OrderStatisticsVO getOrderStatistics(String startDate, String endDate, String groupBy, String serviceTypeCode) {
        OrderStatisticsVO vo = new OrderStatisticsVO();

        Long totalOrders = statisticsMapper.selectTotalOrders();
        Long completedOrders = statisticsMapper.selectCompletedOrders();

        vo.setTotalOrders(totalOrders);
        vo.setCompletedOrders(completedOrders);

        // 计算完成率
        if (totalOrders != null && totalOrders > 0 && completedOrders != null) {
            BigDecimal completionRate = BigDecimal.valueOf(completedOrders)
                    .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            vo.setCompletionRate(completionRate);
        } else {
            vo.setCompletionRate(BigDecimal.ZERO);
        }

        vo.setAverageRating(statisticsMapper.selectAverageRating());
        vo.setTotalAmount(statisticsMapper.selectTotalOrderAmount());

        // 服务类型分布
        vo.setServiceTypeDistribution(convertOrderServiceTypeDistribution(statisticsMapper.selectOrderServiceTypeDistribution()));

        // 订单趋势
        if (startDate != null && endDate != null) {
            vo.setOrderTrend(convertOrderTrendByDateRange(statisticsMapper.selectOrderTrendByDateRange(startDate, endDate, groupBy)));
        } else {
            vo.setOrderTrend(convertOrderTrendForOrderStatistics(statisticsMapper.selectOrderTrendLast7Days()));
        }

        // 订单来源分布
        vo.setOrderSourceDistribution(convertOrderSourceDistribution(statisticsMapper.selectOrderSourceDistribution()));

        return vo;
    }

    @Override
    public FinancialStatisticsVO getFinancialStatistics() {
        FinancialStatisticsVO vo = new FinancialStatisticsVO();

        // 待结算数和已结算数
        vo.setPending(statisticsMapper.selectPendingSettlementCount());
        vo.setCompleted(statisticsMapper.selectCompletedSettlementCount());

        Map<String, Object> summary = statisticsMapper.selectFinancialSummary();
        if (summary != null) {
            vo.setTotalAmount(toBigDecimal(summary.get("totalAmount")));
            vo.setSubsidyTotal(toBigDecimal(summary.get("totalSubsidyAmount")));
            vo.setSelfPayTotal(toBigDecimal(summary.get("totalSelfPayAmount")));
            // 服务费暂按5%计算
            if (vo.getTotalAmount() != null) {
                vo.setServiceFeeTotal(vo.getTotalAmount()
                        .multiply(BigDecimal.valueOf(0.05))
                        .setScale(2, RoundingMode.HALF_UP));
            }
        }

        Map<String, Object> monthlySummary = statisticsMapper.selectMonthlyFinancialSummary();
        if (monthlySummary != null) {
            vo.setMonthAmount(toBigDecimal(monthlySummary.get("monthlyAmount")));
        }

        // 月度趋势
        vo.setMonthlyTrend(convertMonthlyTrend(statisticsMapper.selectMonthlyFinancialTrend()));

        // 服务类型分布
        vo.setServiceTypeDistribution(convertFinancialServiceTypeDistribution(statisticsMapper.selectFinancialServiceTypeDistribution()));

        return vo;
    }

    @Override
    public QualityStatisticsVO getQualityStatistics() {
        QualityStatisticsVO vo = new QualityStatisticsVO();

        Double avgRating = statisticsMapper.selectAverageProviderRating();
        vo.setAverageRating(avgRating != null ? avgRating : 0.0);

        Long totalEvaluations = statisticsMapper.selectTotalEvaluations();
        Long positiveCount = statisticsMapper.selectPositiveEvaluations();
        Long neutralCount = statisticsMapper.selectNeutralEvaluations();
        Long negativeCount = statisticsMapper.selectNegativeEvaluations();

        vo.setTotalEvaluations(totalEvaluations != null ? totalEvaluations : 0L);
        vo.setPositiveCount(positiveCount != null ? positiveCount : 0L);
        vo.setNeutralCount(neutralCount != null ? neutralCount : 0L);
        vo.setNegativeCount(negativeCount != null ? negativeCount : 0L);

        // 计算好评率
        if (totalEvaluations != null && totalEvaluations > 0 && positiveCount != null) {
            BigDecimal positiveRate = BigDecimal.valueOf(positiveCount)
                    .divide(BigDecimal.valueOf(totalEvaluations), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            vo.setPositiveRate(positiveRate);
        } else {
            vo.setPositiveRate(BigDecimal.ZERO);
        }

        // 投诉率暂用差评率代替
        if (negativeCount != null && totalEvaluations != null && totalEvaluations > 0) {
            BigDecimal complaintRate = BigDecimal.valueOf(negativeCount)
                    .divide(BigDecimal.valueOf(totalEvaluations), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            vo.setComplaintRate(complaintRate);
        } else {
            vo.setComplaintRate(BigDecimal.ZERO);
        }

        vo.setComplaintCount(negativeCount != null ? negativeCount : 0L);

        // 评分趋势
        vo.setRatingTrend(convertRatingTrend(statisticsMapper.selectRatingTrendLast7Days()));

        // 常见投诉类型（暂用评分分布代替）
        vo.setComplaintTypes(new ArrayList<>());

        return vo;
    }

    @Override
    public StaffStatisticsVO getStaffStatistics() {
        StaffStatisticsVO vo = new StaffStatisticsVO();
        vo.setTotal(statisticsMapper.selectTotalStaff());
        vo.setActive(statisticsMapper.selectActiveStaff());
        vo.setPending(statisticsMapper.selectPendingStaff());
        vo.setInactive(statisticsMapper.selectInactiveStaff());
        return vo;
    }

    // ==================== 转换方法 ====================

    private List<DashboardVO.ServiceTypeDistribution> convertServiceTypeDistribution(List<Map<String, Object>> data) {
        List<DashboardVO.ServiceTypeDistribution> list = new ArrayList<>();
        if (data == null) return list;

        Long total = statisticsMapper.selectTotalOrders();
        for (Map<String, Object> row : data) {
            DashboardVO.ServiceTypeDistribution item = new DashboardVO.ServiceTypeDistribution();
            item.setServiceTypeCode(toString(row.get("serviceTypeCode")));
            item.setServiceTypeName(toString(row.get("serviceTypeName")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setPercentage(calculatePercentage(toLong(row.get("orderCount")), total));
            list.add(item);
        }
        return list;
    }

    private List<DashboardVO.TrendData> convertOrderTrend(List<Map<String, Object>> data) {
        List<DashboardVO.TrendData> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            DashboardVO.TrendData item = new DashboardVO.TrendData();
            item.setDate(toString(row.get("date")));
            item.setValue(toLong(row.get("orderCount")));
            list.add(item);
        }
        return list;
    }

    private List<OrderStatisticsVO.TrendData> convertOrderTrendForOrderStatistics(List<Map<String, Object>> data) {
        List<OrderStatisticsVO.TrendData> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            OrderStatisticsVO.TrendData item = new OrderStatisticsVO.TrendData();
            item.setDate(toString(row.get("date")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setCompletedCount(0L); // 默认值
            list.add(item);
        }
        return list;
    }

    private List<DashboardVO.TopProvider> convertTopProviders(List<Map<String, Object>> data) {
        List<DashboardVO.TopProvider> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            DashboardVO.TopProvider item = new DashboardVO.TopProvider();
            item.setProviderId(toString(row.get("providerId")));
            item.setProviderName(toString(row.get("providerName")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setRating(toDouble(row.get("rating")));
            list.add(item);
        }
        return list;
    }

    private List<ElderStatisticsVO.AgeDistribution> convertElderAgeDistribution(List<Map<String, Object>> data) {
        List<ElderStatisticsVO.AgeDistribution> list = new ArrayList<>();
        if (data == null) return list;

        Long total = statisticsMapper.selectTotalElders();
        for (Map<String, Object> row : data) {
            ElderStatisticsVO.AgeDistribution item = new ElderStatisticsVO.AgeDistribution();
            item.setAgeRange(toString(row.get("ageRange")));
            item.setLabel(toString(row.get("ageRange")));
            item.setCount(toLong(row.get("count")));
            item.setPercentage(calculatePercentage(toLong(row.get("count")), total));
            list.add(item);
        }
        return list;
    }

    private List<ElderStatisticsVO.CareLevelDistribution> convertCareLevelDistribution(List<Map<String, Object>> data) {
        List<ElderStatisticsVO.CareLevelDistribution> list = new ArrayList<>();
        if (data == null) return list;

        Long total = statisticsMapper.selectTotalElders();
        String[] levelNames = {"不需要护理", "轻度失能", "中度失能", "重度失能"};

        for (Map<String, Object> row : data) {
            ElderStatisticsVO.CareLevelDistribution item = new ElderStatisticsVO.CareLevelDistribution();
            Integer careLevel = toInteger(row.get("careLevel"));
            item.setCareLevel(careLevel);
            item.setLevelName(careLevel != null && careLevel < levelNames.length ? levelNames[careLevel] : "未知");
            item.setCount(toLong(row.get("count")));
            item.setPercentage(calculatePercentage(toLong(row.get("count")), total));
            list.add(item);
        }
        return list;
    }

    private List<ElderStatisticsVO.ServiceDemandDistribution> convertServiceDemandDistribution(List<Map<String, Object>> data) {
        List<ElderStatisticsVO.ServiceDemandDistribution> list = new ArrayList<>();
        if (data == null) return list;

        Long total = statisticsMapper.selectTotalElders();
        String[] typeNames = {"生活照料", "日间照料", "助餐服务", "助洁服务", "助浴服务",
                "健康监测", "康复护理", "精神慰藉", "信息咨询", "紧急救援"};

        for (Map<String, Object> row : data) {
            ElderStatisticsVO.ServiceDemandDistribution item = new ElderStatisticsVO.ServiceDemandDistribution();
            Integer serviceType = toInteger(row.get("serviceType"));
            item.setServiceType(toString(row.get("serviceType")));
            item.setServiceTypeName(serviceType != null && serviceType < typeNames.length ? typeNames[serviceType] : "未知");
            item.setCount(toLong(row.get("count")));
            item.setPercentage(calculatePercentage(toLong(row.get("count")), total));
            list.add(item);
        }
        return list;
    }

    private List<ProviderStatisticsVO.TypeDistribution> convertProviderTypeDistribution(List<Map<String, Object>> data) {
        List<ProviderStatisticsVO.TypeDistribution> list = new ArrayList<>();
        if (data == null) return list;

        Long total = statisticsMapper.selectTotalProviders();

        for (Map<String, Object> row : data) {
            ProviderStatisticsVO.TypeDistribution item = new ProviderStatisticsVO.TypeDistribution();
            item.setProviderType(toString(row.get("providerType")));
            item.setTypeName(toString(row.get("providerType")));
            item.setCount(toLong(row.get("count")));
            item.setPercentage(calculatePercentage(toLong(row.get("count")), total));
            list.add(item);
        }
        return list;
    }

    private List<ProviderStatisticsVO.ProviderRanking> convertProviderRankings(List<Map<String, Object>> data) {
        List<ProviderStatisticsVO.ProviderRanking> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            ProviderStatisticsVO.ProviderRanking item = new ProviderStatisticsVO.ProviderRanking();
            item.setProviderId(toString(row.get("providerId")));
            item.setProviderName(toString(row.get("providerName")));
            item.setProviderType(toString(row.get("providerType")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setCompletedOrderCount(toLong(row.get("completedOrderCount")));
            item.setRating(toDouble(row.get("rating")));

            Long total = toLong(row.get("orderCount"));
            Long completed = toLong(row.get("completedOrderCount"));
            if (total != null && total > 0 && completed != null) {
                item.setCompletionRate(BigDecimal.valueOf(completed)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue());
            } else {
                item.setCompletionRate(0.0);
            }
            list.add(item);
        }
        return list;
    }

    private List<OrderStatisticsVO.ServiceTypeDistribution> convertOrderServiceTypeDistribution(List<Map<String, Object>> data) {
        List<OrderStatisticsVO.ServiceTypeDistribution> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            OrderStatisticsVO.ServiceTypeDistribution item = new OrderStatisticsVO.ServiceTypeDistribution();
            item.setServiceTypeCode(toString(row.get("serviceTypeCode")));
            item.setServiceTypeName(toString(row.get("serviceTypeName")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setCompletedCount(toLong(row.get("completedCount")));
            item.setTotalAmount(toBigDecimal(row.get("totalAmount")));

            Long total = toLong(row.get("orderCount"));
            Long completed = toLong(row.get("completedCount"));
            if (total != null && total > 0 && completed != null) {
                item.setCompletionRate(BigDecimal.valueOf(completed)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                item.setCompletionRate(BigDecimal.ZERO);
            }
            list.add(item);
        }
        return list;
    }

    private List<OrderStatisticsVO.TrendData> convertOrderTrendByDateRange(List<Map<String, Object>> data) {
        List<OrderStatisticsVO.TrendData> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            OrderStatisticsVO.TrendData item = new OrderStatisticsVO.TrendData();
            item.setDate(toString(row.get("date")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setCompletedCount(toLong(row.get("completedCount")));
            list.add(item);
        }
        return list;
    }

    private List<OrderStatisticsVO.OrderSourceDistribution> convertOrderSourceDistribution(List<Map<String, Object>> data) {
        List<OrderStatisticsVO.OrderSourceDistribution> list = new ArrayList<>();
        if (data == null) return list;

        Long total = statisticsMapper.selectTotalOrders();

        for (Map<String, Object> row : data) {
            OrderStatisticsVO.OrderSourceDistribution item = new OrderStatisticsVO.OrderSourceDistribution();
            item.setOrderSource(toString(row.get("orderSource")));
            item.setSourceName(toString(row.get("orderSource")));
            item.setCount(toLong(row.get("count")));
            item.setPercentage(calculatePercentage(toLong(row.get("count")), total));
            list.add(item);
        }
        return list;
    }

    private List<FinancialStatisticsVO.MonthlyTrend> convertMonthlyTrend(List<Map<String, Object>> data) {
        List<FinancialStatisticsVO.MonthlyTrend> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            FinancialStatisticsVO.MonthlyTrend item = new FinancialStatisticsVO.MonthlyTrend();
            item.setMonth(toString(row.get("month")));
            item.setTotalAmount(toBigDecimal(row.get("totalAmount")));
            item.setSubsidyAmount(toBigDecimal(row.get("subsidyAmount")));
            item.setSelfPayAmount(toBigDecimal(row.get("selfPayAmount")));
            item.setOrderCount(toLong(row.get("orderCount")));
            list.add(item);
        }
        return list;
    }

    private List<FinancialStatisticsVO.ServiceTypeDistribution> convertFinancialServiceTypeDistribution(List<Map<String, Object>> data) {
        List<FinancialStatisticsVO.ServiceTypeDistribution> list = new ArrayList<>();
        if (data == null) return list;

        BigDecimal total = statisticsMapper.selectTotalOrderAmount();

        for (Map<String, Object> row : data) {
            FinancialStatisticsVO.ServiceTypeDistribution item = new FinancialStatisticsVO.ServiceTypeDistribution();
            item.setServiceTypeCode(toString(row.get("serviceTypeCode")));
            item.setServiceTypeName(toString(row.get("serviceTypeName")));
            item.setTotalAmount(toBigDecimal(row.get("totalAmount")));
            item.setSubsidyAmount(toBigDecimal(row.get("subsidyAmount")));
            item.setSelfPayAmount(toBigDecimal(row.get("selfPayAmount")));
            item.setOrderCount(toLong(row.get("orderCount")));
            item.setPercentage(calculatePercentage(toBigDecimal(row.get("totalAmount")), total));
            list.add(item);
        }
        return list;
    }

    private List<QualityStatisticsVO.RatingTrend> convertRatingTrend(List<Map<String, Object>> data) {
        List<QualityStatisticsVO.RatingTrend> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> row : data) {
            QualityStatisticsVO.RatingTrend item = new QualityStatisticsVO.RatingTrend();
            item.setDate(toString(row.get("date")));
            item.setAverageRating(toDouble(row.get("averageRating")));
            item.setEvaluationCount(toLong(row.get("evaluationCount")));
            list.add(item);
        }
        return list;
    }

    // ==================== 工具方法 ====================

    private BigDecimal calculatePercentage(Long count, Long total) {
        if (total == null || total == 0 || count == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(count)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentage(BigDecimal count, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0 || count == null) {
            return BigDecimal.ZERO;
        }
        return count.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Float) return ((Float) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Long) return BigDecimal.valueOf((Long) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String toString(Object value) {
        return value == null ? "" : value.toString();
    }
}
