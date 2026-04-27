package com.elderlycare.controller.staff;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.staff.StaffScore;
import com.elderlycare.service.staff.StaffScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务人员评分Controller
 */
@RestController
@RequestMapping("/api/staff-score")
@RequiredArgsConstructor
public class StaffScoreController {

    private final StaffScoreService staffScoreService;

    /**
     * 生成服务人员评分
     */
    @PostMapping("")
    public Result<Void> generateScore(@RequestParam String staffId, @RequestParam String periodType) {
        staffScoreService.saveOrUpdateScore(staffId, periodType);
        return Result.successMsg("评分生成成功");
    }

    /**
     * 获取服务人员评分列表
     */
    @GetMapping("/list")
    public Result<PageResult<StaffScore>> getScoreList(
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String periodType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 数据权限：服务商管理员只能查看自己服务商的评分
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            providerId = autoPid;
        }
        PageResult<StaffScore> result = staffScoreService.getScoreList(staffId, providerId, periodType, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取服务人员最新评分
     */
    @GetMapping("/latest")
    public Result<StaffScore> getLatestScore(@RequestParam String staffId) {
        StaffScore score = staffScoreService.getLatestScore(staffId);
        return Result.success(score);
    }

    /**
     * 获取服务人员评分趋势
     */
    @GetMapping("/trend")
    public Result<List<StaffScore>> getScoreTrend(
            @RequestParam String staffId,
            @RequestParam(required = false) String periodType,
            @RequestParam(defaultValue = "6") int months) {
        List<StaffScore> trend = staffScoreService.getScoreTrend(staffId, periodType, months);
        return Result.success(trend);
    }
}
