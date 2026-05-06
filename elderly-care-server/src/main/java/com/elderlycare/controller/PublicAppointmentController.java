package com.elderlycare.controller;

import com.elderlycare.common.Result;
import com.elderlycare.dto.appointment.PublicAppointmentSubmitDTO;
import com.elderlycare.service.appointment.AppointmentService;
import com.elderlycare.vo.appointment.PublicAppointmentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开预约Controller（不需要JWT认证）
 * 路径: /public/appointment
 */
@RestController
@RequestMapping("/public/appointment")
@RequiredArgsConstructor
public class PublicAppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 验证Token获取预约表单信息
     * GET /public/appointment?token=xxx
     */
    @GetMapping
    public Result<PublicAppointmentVO> getInfo(@RequestParam String token) {
        return Result.success(appointmentService.validateAppointmentToken(token));
    }

    /**
     * 提交预约
     * POST /public/appointment/submit?token=xxx
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestParam String token,
                                 @Valid @RequestBody PublicAppointmentSubmitDTO dto) {
        String appointmentId = appointmentService.submitPublicAppointment(token, dto);
        return Result.success(appointmentId);
    }
}
