package com.elderlycare.controller.quality;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.quality.QualityImprovement;
import com.elderlycare.service.quality.QualityImprovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量改进Controller
 */
@RestController
@RequestMapping("/api/quality-improvement")
@RequiredArgsConstructor
public class QualityImprovementController {

    private final QualityImprovementService improvementService;

    /**
     * 创建改进计划
     */
    @PostMapping("")
    public Result<String> createImprovement(@RequestBody QualityImprovement improvement) {
        String improvementId = improvementService.createImprovement(improvement);
        return Result.success(improvementId);
    }

    /**
     * 获取改进列表
     */
    @GetMapping("/list")
    public Result<PageResult<QualityImprovement>> getImprovementList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 数据权限：服务商管理员只能查看自己的改进计划
        String userType = UserContext.getUserType();
        String providerId = null;
        if ("PROVIDER".equals(userType)) {
            providerId = UserContext.getProviderId();
        }
        PageResult<QualityImprovement> result = improvementService.getImprovementList(providerId, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取改进详情
     */
    @GetMapping("/{improvementId}")
    public Result<QualityImprovement> getImprovementById(@PathVariable String improvementId) {
        QualityImprovement improvement = improvementService.getImprovementById(improvementId);
        return Result.success(improvement);
    }

    /**
     * 更新改进计划
     */
    @PutMapping("/{improvementId}")
    public Result<Void> updateImprovement(@PathVariable String improvementId, @RequestBody QualityImprovement improvement) {
        improvement.setImprovementId(improvementId);
        improvementService.updateImprovement(improvement);
        return Result.success("更新成功");
    }

    /**
     * 开始执行
     */
    @PutMapping("/{improvementId}/start")
    public Result<Void> startExecution(@PathVariable String improvementId) {
        improvementService.startExecution(improvementId);
        return Result.success("已开始执行");
    }

    /**
     * 标记完成
     */
    @PutMapping("/{improvementId}/complete")
    public Result<Void> complete(@PathVariable String improvementId) {
        improvementService.complete(improvementId);
        return Result.success("已标记完成");
    }

    /**
     * 评估效果
     */
    @PutMapping("/{improvementId}/evaluate")
    public Result<Void> evaluate(@PathVariable String improvementId, @RequestBody java.util.Map<String, String> params) {
        String effectEvaluation = params.get("effectEvaluation");
        String evaluationResult = params.get("evaluationResult");
        improvementService.evaluate(improvementId, effectEvaluation, evaluationResult);
        return Result.success("评估完成");
    }
}
