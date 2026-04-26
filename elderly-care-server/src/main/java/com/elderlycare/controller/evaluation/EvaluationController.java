package com.elderlycare.controller.evaluation;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.CustomerFeedback;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.order.OrderMapper;
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
    private final OrderMapper orderMapper;

    // ==================== 服务评价接口 ====================

    /**
     * 评价提交
     * 隔离：PROVIDER只能为自己公司的订单创建评价
     */
    @PostMapping("")
    public Result<String> createEvaluation(@Validated @RequestBody CreateEvaluationDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            Order order = orderMapper.selectById(dto.getOrderId());
            if (order != null && !autoPid.equals(order.getProviderId())) {
                throw BusinessException.forbidden("无权为他方订单创建评价");
            }
        }
        String evaluationId = evaluationService.createEvaluation(dto);
        return Result.success(evaluationId);
    }

    /**
     * 评价列表
     */
    @GetMapping("")
    public Result<PageResult<ServiceEvaluation>> queryEvaluations(EvaluationQueryDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            dto.setProviderId(autoPid);
            dto.setStaffId(null);
        }
        String staffId = UserContext.getStaffId();
        if ("STAFF".equals(userType) && staffId != null) {
            dto.setStaffId(staffId);
            dto.setProviderId(null);
        }
        PageResult<ServiceEvaluation> result = evaluationService.queryEvaluations(dto);
        return Result.success(result);
    }

    /**
     * 评价详情
     * 隔离规则：PROVIDER只能查自己公司的评价
     */
    @GetMapping("/{evaluationId}")
    public Result<EvaluationVO> getEvaluationById(@PathVariable String evaluationId) {
        EvaluationVO vo = evaluationService.getEvaluationById(evaluationId);
        // 隔离校验：PROVIDER用户只能看自己公司的评价
        String userType = UserContext.getUserType();
        String myProviderId = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && myProviderId != null && vo != null) {
            if (!myProviderId.equals(vo.getProviderId())) {
                throw BusinessException.fail("无权访问其他公司的评价信息");
            }
        }
        return Result.success(vo);
    }

    /**
     * 根据订单ID查询评价（用于质检详情页关联展示）
     */
    @GetMapping("/order/{orderId}")
    public Result<ServiceEvaluation> getEvaluationByOrderId(@PathVariable String orderId) {
        ServiceEvaluation evaluation = evaluationService.getEvaluationByOrderId(orderId);
        return Result.success(evaluation);
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
            @RequestBody java.util.Map<String, String> body) {
        String replyContent = body.get("reply");
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

    // ==================== 问卷邀请接口（公开） ====================

    /**
     * 生成评价邀请链接
     */
    @PostMapping("/generate-link")
    public Result<com.elderlycare.vo.evaluation.EvaluationInviteVO> generateEvaluationLink(
            @RequestParam String orderId,
            @RequestParam String elderId,
            @RequestParam String elderName,
            @RequestParam(required = false, defaultValue = "72") Integer expireHours) {
        com.elderlycare.vo.evaluation.EvaluationInviteVO vo = evaluationService.generateEvaluationLink(
            orderId, elderId, elderName, expireHours);
        return Result.success(vo);
    }

    /**
     * 验证Token获取问卷信息（公开接口）
     */
    @GetMapping("/survey/{token}")
    public Result<com.elderlycare.vo.evaluation.EvaluationInviteVO> getSurveyInfo(@PathVariable String token) {
        com.elderlycare.vo.evaluation.EvaluationInviteVO vo = evaluationService.validateToken(token);
        return Result.success(vo);
    }

    /**
     * 提交问卷评价（公开接口）
     */
    @PostMapping("/survey/{token}/submit")
    public Result<Void> submitSurvey(
            @PathVariable String token,
            @Validated @RequestBody SubmitSurveyDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress = forwardedFor;
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        evaluationService.submitSurveyByToken(token, dto, ipAddress);
        return Result.success();
    }

    /**
     * 作废评价邀请链接
     */
    @PutMapping("/invite/{token}/invalidate")
    public Result<Void> invalidateInvite(@PathVariable String token) {
        evaluationService.invalidateInvite(token);
        return Result.success();
    }
}
