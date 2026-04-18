package com.elderlycare.controller.evaluation;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.CustomerFeedback;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.service.evaluation.CustomerFeedbackService;
import com.elderlycare.service.evaluation.ServiceEvaluationService;
import com.elderlycare.vo.evaluation.EvaluationStatisticsVO;
import com.elderlycare.vo.evaluation.EvaluationVO;
import com.elderlycare.vo.evaluation.FeedbackVO;
import com.elderlycare.vo.evaluation.ProviderScoreVO;
import com.elderlycare.vo.evaluation.StaffScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务评价Controller
 */
@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final ServiceEvaluationService evaluationService;
    private final CustomerFeedbackService feedbackService;

    // ==================== 服务评价接口 ====================

    /**
     * 评价提交
     */
    @PostMapping("")
    public Result<String> createEvaluation(@Validated @RequestBody CreateEvaluationDTO dto) {
        String evaluationId = evaluationService.createEvaluation(dto);
        return Result.success(evaluationId);
    }

    /**
     * 评价列表
     */
    @GetMapping("")
    public Result<PageResult<ServiceEvaluation>> queryEvaluations(EvaluationQueryDTO dto) {
        // 数据权限：服务商管理员自动注入 providerId
        String autoPid = UserContext.getProviderId();
        if (autoPid != null) {
            dto.setProviderId(autoPid);
        }
        PageResult<ServiceEvaluation> result = evaluationService.queryEvaluations(dto);
        return Result.success(result);
    }

    /**
     * 评价详情
     */
    @GetMapping("/{evaluationId}")
    public Result<EvaluationVO> getEvaluationById(@PathVariable String evaluationId) {
        EvaluationVO vo = evaluationService.getEvaluationById(evaluationId);
        return Result.success(vo);
    }

    /**
     * 服务商评分查询
     */
    @GetMapping("/provider-score")
    public Result<ProviderScoreVO> getProviderScore(@RequestParam String providerId) {
        ProviderScoreVO vo = evaluationService.getProviderScore(providerId);
        return Result.success(vo);
    }

    /**
     * 服务人员评分查询
     */
    @GetMapping("/staff-score/{staffId}")
    public Result<StaffScoreVO> getStaffScore(@PathVariable String staffId) {
        StaffScoreVO vo = evaluationService.getStaffScore(staffId);
        return Result.success(vo);
    }

    /**
     * 评价统计
     */
    @GetMapping("/statistics")
    public Result<EvaluationStatisticsVO> getStatistics(EvaluationStatisticsDTO dto) {
        // 数据权限：服务商管理员自动注入 providerId
        String autoPid = UserContext.getProviderId();
        if (autoPid != null) {
            dto.setProviderId(autoPid);
        }
        EvaluationStatisticsVO vo = evaluationService.getStatistics(dto);
        return Result.success(vo);
    }

    /**
     * 回复评价
     */
    @PutMapping("/{evaluationId}/reply")
    public Result<Void> replyEvaluation(
            @PathVariable String evaluationId,
            @RequestBody String replyContent) {
        evaluationService.replyEvaluation(evaluationId, replyContent);
        return Result.success();
    }

    // ==================== 客户反馈接口 ====================

    /**
     * 反馈提交
     */
    @PostMapping("/feedback")
    public Result<String> createFeedback(@Validated @RequestBody CreateFeedbackDTO dto) {
        String feedbackId = feedbackService.createFeedback(dto);
        return Result.success(feedbackId);
    }

    /**
     * 反馈列表
     */
    @GetMapping("/feedback")
    public Result<PageResult<CustomerFeedback>> queryFeedbacks(FeedbackQueryDTO dto) {
        // 数据权限：服务商管理员自动注入 providerId
        String autoPid = UserContext.getProviderId();
        if (autoPid != null) {
            dto.setProviderId(autoPid);
        }
        PageResult<CustomerFeedback> result = feedbackService.queryFeedbacks(dto);
        return Result.success(result);
    }

    /**
     * 反馈详情
     */
    @GetMapping("/feedback/{feedbackId}")
    public Result<FeedbackVO> getFeedbackById(@PathVariable String feedbackId) {
        FeedbackVO vo = feedbackService.getFeedbackById(feedbackId);
        return Result.success(vo);
    }

    /**
     * 反馈处理
     */
    @PutMapping("/feedback/{feedbackId}/handle")
    public Result<Void> handleFeedback(
            @PathVariable String feedbackId,
            @Validated @RequestBody HandleFeedbackDTO dto) {
        feedbackService.handleFeedback(feedbackId, dto);
        return Result.success();
    }
}
