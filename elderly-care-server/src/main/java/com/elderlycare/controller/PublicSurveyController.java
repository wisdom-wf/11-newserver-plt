package com.elderlycare.controller;

import com.elderlycare.common.Result;
import com.elderlycare.dto.evaluation.SubmitSurveyDTO;
import com.elderlycare.service.evaluation.ServiceEvaluationService;
import com.elderlycare.vo.evaluation.EvaluationInviteVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开评价问卷Controller（不需要JWT认证）
 * 路径: /public/survey
 */
@RestController
@RequestMapping("/public/survey")
@RequiredArgsConstructor
public class PublicSurveyController {

    private final ServiceEvaluationService evaluationService;

    /**
     * 验证Token获取问卷信息（公开接口）
     * GET /public/survey?token=xxx
     */
    @GetMapping
    public Result<EvaluationInviteVO> getSurveyInfo(@RequestParam String token) {
        EvaluationInviteVO vo = evaluationService.validateToken(token);
        return Result.success(vo);
    }

    /**
     * 提交问卷评价（公开接口）
     * POST /public/survey/submit?token=xxx
     */
    @PostMapping("/submit")
    public Result<Void> submitSurvey(
            @RequestParam String token,
            @Valid @RequestBody SubmitSurveyDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            HttpServletRequest request) {
        String ipAddress = forwardedFor;
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        evaluationService.submitSurveyByToken(token, dto, ipAddress);
        return Result.success();
    }
}
