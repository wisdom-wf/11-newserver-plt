package com.elderlycare.controller.cockpit;

import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.service.cockpit.CockpitService;
import com.elderlycare.vo.cockpit.CockpitOverviewVO;
import com.elderlycare.vo.statistics.OrderStatisticsVO;
import com.elderlycare.vo.statistics.FinancialStatisticsVO;
import com.elderlycare.vo.statistics.ElderStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 驾驶舱Controller
 */
@RestController
@RequestMapping("/api/cockpit")
@RequiredArgsConstructor
public class CockpitController {

    private final CockpitService cockpitService;

    /**
     * 获取驾驶舱概览
     */
    @GetMapping("/overview")
    public Result<CockpitOverviewVO> getOverview(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // 数据权限：服务商管理员自动注入 providerId
        String autoPid = UserContext.getProviderId();
        String effectivePid = (autoPid != null) ? autoPid : providerId;
        CockpitOverviewVO overview = cockpitService.getOverview();
        return Result.success(overview);
    }

    /**
     * 获取订单趋势
     */
    @GetMapping("/orderTrend")
    public Result<List<OrderStatisticsVO.TrendData>> getOrderTrend(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "day") String type) {
        List<OrderStatisticsVO.TrendData> trend = cockpitService.getOrderTrend(type);
        return Result.success(trend);
    }

    /**
     * 获取服务类型分布
     */
    @GetMapping("/serviceDistribution")
    public Result<List<CockpitOverviewVO.ServiceDistribution>> getServiceDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        List<CockpitOverviewVO.ServiceDistribution> distribution = cockpitService.getServiceDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取区域分布
     */
    @GetMapping("/areaDistribution")
    public Result<List<CockpitOverviewVO.AreaDistribution>> getAreaDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        List<CockpitOverviewVO.AreaDistribution> distribution = cockpitService.getAreaDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取服务商排行
     */
    @GetMapping("/providerRanking")
    public Result<List<CockpitOverviewVO.ProviderRanking>> getProviderRanking(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "order") String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<CockpitOverviewVO.ProviderRanking> ranking = cockpitService.getProviderRanking(type, limit);
        return Result.success(ranking);
    }

    /**
     * 获取服务人员排行
     */
    @GetMapping("/staffRanking")
    public Result<List<CockpitOverviewVO.StaffRanking>> getStaffRanking(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "order") String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<CockpitOverviewVO.StaffRanking> ranking = cockpitService.getStaffRanking(type, limit);
        return Result.success(ranking);
    }

    /**
     * 获取满意度分布
     */
    @GetMapping("/satisfactionDistribution")
    public Result<Map<String, Long>> getSatisfactionDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        Map<String, Long> distribution = cockpitService.getSatisfactionDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取质检分布
     */
    @GetMapping("/qualityDistribution")
    public Result<Map<String, Long>> getQualityDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        Map<String, Long> distribution = cockpitService.getQualityDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取财务趋势
     */
    @GetMapping("/financialTrend")
    public Result<List<FinancialStatisticsVO.MonthlyTrend>> getFinancialTrend(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "day") String type) {
        List<FinancialStatisticsVO.MonthlyTrend> trend = cockpitService.getFinancialTrend(type);
        return Result.success(trend);
    }

    /**
     * 获取年龄段分布
     */
    @GetMapping("/ageDistribution")
    public Result<List<ElderStatisticsVO.AgeDistribution>> getAgeDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        List<ElderStatisticsVO.AgeDistribution> distribution = cockpitService.getAgeDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取护理等级分布
     */
    @GetMapping("/careLevelDistribution")
    public Result<List<ElderStatisticsVO.CareLevelDistribution>> getCareLevelDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        List<ElderStatisticsVO.CareLevelDistribution> distribution = cockpitService.getCareLevelDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取实时订单
     */
    @GetMapping("/realtimeOrders")
    public Result<Object> getRealtimeOrders(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(cockpitService.getRealtimeOrders(limit));
    }

    /**
     * 获取预警信息
     */
    @GetMapping("/warnings")
    public Result<Object> getWarnings(
            @RequestParam(required = false) String level,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(cockpitService.getWarnings(level, limit));
    }

    /**
     * 获取热力图数据
     */
    @GetMapping("/heatMapData")
    public Result<Map<String, Object>> getHeatMapData(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        Map<String, Object> data = cockpitService.getHeatMapData();
        return Result.success(data);
    }
}
