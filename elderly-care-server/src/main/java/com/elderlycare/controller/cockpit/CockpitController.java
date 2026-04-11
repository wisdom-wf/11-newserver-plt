package com.elderlycare.controller.cockpit;

import com.elderlycare.common.Result;
import com.elderlycare.service.cockpit.CockpitService;
import com.elderlycare.vo.cockpit.CockpitOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
        CockpitOverviewVO overview = cockpitService.getOverview();
        return Result.success(overview);
    }

    /**
     * 获取订单趋势
     */
    @GetMapping("/orderTrend")
    public Result<Object> getOrderTrend(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "day") String type) {
        // TODO: 实现订单趋势
        return Result.success(null);
    }

    /**
     * 获取服务类型分布
     */
    @GetMapping("/serviceDistribution")
    public Result<Object> getServiceDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        // TODO: 实现服务类型分布
        return Result.success(null);
    }

    /**
     * 获取区域分布
     */
    @GetMapping("/areaDistribution")
    public Result<Object> getAreaDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        // TODO: 实现区域分布
        return Result.success(null);
    }

    /**
     * 获取服务商排行
     */
    @GetMapping("/providerRanking")
    public Result<Object> getProviderRanking(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "order") String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        // TODO: 实现服务商排行
        return Result.success(null);
    }

    /**
     * 获取服务人员排行
     */
    @GetMapping("/staffRanking")
    public Result<Object> getStaffRanking(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "order") String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        // TODO: 实现服务人员排行
        return Result.success(null);
    }

    /**
     * 获取满意度分布
     */
    @GetMapping("/satisfactionDistribution")
    public Result<Object> getSatisfactionDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        // TODO: 实现满意度分布
        return Result.success(null);
    }

    /**
     * 获取质检分布
     */
    @GetMapping("/qualityDistribution")
    public Result<Object> getQualityDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        // TODO: 实现质检分布
        return Result.success(null);
    }

    /**
     * 获取财务趋势
     */
    @GetMapping("/financialTrend")
    public Result<Object> getFinancialTrend(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false, defaultValue = "day") String type) {
        // TODO: 实现财务趋势
        return Result.success(null);
    }

    /**
     * 获取年龄段分布
     */
    @GetMapping("/ageDistribution")
    public Result<Object> getAgeDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        // TODO: 实现年龄段分布
        return Result.success(null);
    }

    /**
     * 获取护理等级分布
     */
    @GetMapping("/careLevelDistribution")
    public Result<Object> getCareLevelDistribution(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId) {
        // TODO: 实现护理等级分布
        return Result.success(null);
    }

    /**
     * 获取实时订单
     */
    @GetMapping("/realtimeOrders")
    public Result<Object> getRealtimeOrders(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        // TODO: 实现实时订单
        return Result.success(null);
    }

    /**
     * 获取预警信息
     */
    @GetMapping("/warnings")
    public Result<Object> getWarnings(
            @RequestParam(required = false) String level,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        // TODO: 实现预警信息
        return Result.success(null);
    }
}
