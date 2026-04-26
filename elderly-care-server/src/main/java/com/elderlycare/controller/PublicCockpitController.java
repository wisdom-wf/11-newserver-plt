package com.elderlycare.controller;

import com.elderlycare.common.Result;
import com.elderlycare.service.PublicTokenService;
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
 * 公开驾驶舱Controller
 * 无需登录即可访问，用于数据大屏和分享
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCockpitController {

    private final PublicTokenService publicTokenService;
    private final CockpitService cockpitService;

    /**
     * 生成公开访问Token
     * @param type Token类型：cockpit
     * @param expiresInSeconds 过期时间（秒），默认86400（24小时）
     * @return Token信息
     */
    @PostMapping("/token")
    public Result<TokenVO> generateToken(
            @RequestParam(defaultValue = "cockpit") String type,
            @RequestParam(defaultValue = "86400") long expiresInSeconds) {
        String token = publicTokenService.generateToken(type, expiresInSeconds);
        return Result.success(new TokenVO(token, expiresInSeconds));
    }

    /**
     * 验证Token
     */
    @GetMapping("/token/validate")
    public Result<Boolean> validateToken(@RequestParam String token) {
        boolean valid = publicTokenService.validateToken(token);
        return Result.success(valid);
    }

    /**
     * 撤销Token
     */
    @DeleteMapping("/token")
    public Result<Void> revokeToken(@RequestParam String token) {
        publicTokenService.revokeToken(token);
        return Result.success();
    }

    /**
     * 获取驾驶舱概览（公开）
     */
    @GetMapping("/cockpit/overview")
    public Result<CockpitOverviewVO> getOverview(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        validateTokenIfPresent(token);
        CockpitOverviewVO overview = cockpitService.getOverview(null);
        return Result.success(overview);
    }

    /**
     * 获取订单趋势（公开）
     */
    @GetMapping("/cockpit/orderTrend")
    public Result<List<OrderStatisticsVO.TrendData>> getOrderTrend(
            @RequestParam(required = false) String token,
            @RequestParam(required = false, defaultValue = "day") String type) {
        validateTokenIfPresent(token);
        List<OrderStatisticsVO.TrendData> trend = cockpitService.getOrderTrend(null, type);
        return Result.success(trend);
    }

    /**
     * 获取服务商排行（公开）
     */
    @GetMapping("/cockpit/providerRanking")
    public Result<List<CockpitOverviewVO.ProviderRanking>> getProviderRanking(
            @RequestParam(required = false) String token,
            @RequestParam(required = false, defaultValue = "order") String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        validateTokenIfPresent(token);
        List<CockpitOverviewVO.ProviderRanking> ranking = cockpitService.getProviderRanking(null, type, limit);
        return Result.success(ranking);
    }

    /**
     * 获取服务类型分布（公开）
     */
    @GetMapping("/cockpit/serviceDistribution")
    public Result<List<CockpitOverviewVO.ServiceDistribution>> getServiceDistribution(
            @RequestParam(required = false) String token) {
        validateTokenIfPresent(token);
        List<CockpitOverviewVO.ServiceDistribution> distribution = cockpitService.getServiceDistribution(null);
        return Result.success(distribution);
    }

    /**
     * 获取区域分布（公开）
     */
    @GetMapping("/cockpit/areaDistribution")
    public Result<List<CockpitOverviewVO.AreaDistribution>> getAreaDistribution(
            @RequestParam(required = false) String token) {
        validateTokenIfPresent(token);
        List<CockpitOverviewVO.AreaDistribution> distribution = cockpitService.getAreaDistribution(null);
        return Result.success(distribution);
    }

    /**
     * 获取年龄段分布（公开）
     */
    @GetMapping("/cockpit/ageDistribution")
    public Result<List<ElderStatisticsVO.AgeDistribution>> getAgeDistribution(
            @RequestParam(required = false) String token) {
        validateTokenIfPresent(token);
        List<ElderStatisticsVO.AgeDistribution> distribution = cockpitService.getAgeDistribution(null);
        return Result.success(distribution);
    }

    /**
     * 获取护理等级分布（公开）
     */
    @GetMapping("/cockpit/careLevelDistribution")
    public Result<List<ElderStatisticsVO.CareLevelDistribution>> getCareLevelDistribution(
            @RequestParam(required = false) String token) {
        validateTokenIfPresent(token);
        List<ElderStatisticsVO.CareLevelDistribution> distribution = cockpitService.getCareLevelDistribution(null);
        return Result.success(distribution);
    }

    /**
     * 获取实时订单（公开）
     */
    @GetMapping("/cockpit/realtimeOrders")
    public Result<Object> getRealtimeOrders(
            @RequestParam(required = false) String token,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        validateTokenIfPresent(token);
        return Result.success(cockpitService.getRealtimeOrders(null, limit));
    }

    private void validateTokenIfPresent(String token) {
        if (token != null && !token.isEmpty()) {
            if (!publicTokenService.validateToken(token)) {
                throw new com.elderlycare.common.BusinessException(401, "Token无效或已过期");
            }
        }
    }

    /**
     * Token VO
     */
    public record TokenVO(String token, long expiresInSeconds) {}
}
