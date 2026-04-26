package com.elderlycare.controller.provider;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.provider.ProviderScore;
import com.elderlycare.service.provider.ProviderScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务商评分Controller
 */
@RestController
@RequestMapping("/api/provider-score")
@RequiredArgsConstructor
public class ProviderScoreController {

    private final ProviderScoreService providerScoreService;

    /**
     * 生成服务商评分
     */
    @PostMapping("")
    public Result<Void> generateScore(@RequestParam String providerId, @RequestParam String periodType) {
        providerScoreService.saveOrUpdateScore(providerId, periodType);
        return Result.success("评分生成成功");
    }

    /**
     * 获取服务商评分列表
     */
    @GetMapping("/list")
    public Result<PageResult<ProviderScore>> getScoreList(
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String periodType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 数据权限：服务商管理员只能查看自己的评分
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            providerId = autoPid;
        }
        PageResult<ProviderScore> result = providerScoreService.getScoreList(providerId, periodType, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取服务商最新评分
     */
    @GetMapping("/latest")
    public Result<ProviderScore> getLatestScore(@RequestParam String providerId) {
        ProviderScore score = providerScoreService.getLatestScore(providerId);
        return Result.success(score);
    }

    /**
     * 获取服务商评分趋势
     */
    @GetMapping("/trend")
    public Result<List<ProviderScore>> getScoreTrend(
            @RequestParam String providerId,
            @RequestParam(required = false) String periodType,
            @RequestParam(defaultValue = "6") int months) {
        List<ProviderScore> trend = providerScoreService.getScoreTrend(providerId, periodType, months);
        return Result.success(trend);
    }
}
