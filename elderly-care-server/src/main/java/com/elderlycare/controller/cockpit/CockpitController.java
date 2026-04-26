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
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getOverview(effectivePid));
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
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getOrderTrend(effectivePid, type));
    }

    /**
     * 获取服务类型分布
     */
    @GetMapping("/serviceDistribution")
    public Result<List<CockpitOverviewVO.ServiceDistribution>> getServiceDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getServiceDistribution(effectivePid));
    }

    /**
     * 获取区域分布
     */
    @GetMapping("/areaDistribution")
    public Result<List<CockpitOverviewVO.AreaDistribution>> getAreaDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getAreaDistribution(effectivePid));
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
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getProviderRanking(effectivePid, type, limit));
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
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getStaffRanking(effectivePid, type, limit));
    }

    /**
     * 获取满意度分布
     */
    @GetMapping("/satisfactionDistribution")
    public Result<Map<String, Long>> getSatisfactionDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getSatisfactionDistribution(effectivePid));
    }

    /**
     * 获取质检分布
     */
    @GetMapping("/qualityDistribution")
    public Result<Map<String, Long>> getQualityDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getQualityDistribution(effectivePid));
    }

    /**
     * 获取财务趋势
     */
    @GetMapping("/financialTrend")
    public Result<List<FinancialStatisticsVO.MonthlyTrend>> getFinancialTrend(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "day") String type) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getFinancialTrend(effectivePid, type));
    }

    /**
     * 获取年龄段分布
     */
    @GetMapping("/ageDistribution")
    public Result<List<ElderStatisticsVO.AgeDistribution>> getAgeDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getAgeDistribution(effectivePid));
    }

    /**
     * 获取护理等级分布
     */
    @GetMapping("/careLevelDistribution")
    public Result<List<ElderStatisticsVO.CareLevelDistribution>> getCareLevelDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getCareLevelDistribution(effectivePid));
    }

    /**
     * 获取实时订单
     */
    @GetMapping("/realtimeOrders")
    public Result<Object> getRealtimeOrders(
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getRealtimeOrders(effectivePid, limit));
    }

    /**
     * 获取预警信息
     */
    @GetMapping("/warnings")
    public Result<Object> getWarnings(
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getWarnings(effectivePid, level, limit));
    }

    /**
     * 获取热力图数据
     */
    @GetMapping("/heatMapData")
    public Result<Map<String, Object>> getHeatMapData(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        String effectivePid = resolveProviderId(providerId);
        return Result.success(cockpitService.getHeatMapData(effectivePid));
    }

    /**
     * 解析effectiveProviderId。
     * - PROVIDER用户：强制使用UserContext中的providerId（防止前端伪造）
     * - ADMIN/CITY_ADMIN：使用前端传入的providerId参数（可选过滤）
     */
    private String resolveProviderId(String paramProviderId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            return autoPid; // 强制使用自己的providerId
        }
        return paramProviderId; // ADMIN/CITY_ADMIN可指定过滤
    }
}
