package com.elderlycare.controller.appointment;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.appointment.AppointmentQueryDTO;
import com.elderlycare.service.appointment.AppointmentService;
import com.elderlycare.vo.appointment.AppointmentStatisticsVO;
import com.elderlycare.vo.appointment.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 预约管理控制器
 */
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 获取预约列表
     * GET /api/appointment/list
     */
    @GetMapping("/list")
    public Result<PageResult<AppointmentVO>> getAppointmentList(AppointmentQueryDTO query) {
        PageResult<AppointmentVO> result = appointmentService.getAppointmentList(query);
        return Result.success(result);
    }

    /**
     * 获取预约详情
     * GET /api/appointment/{id}
     */
    @GetMapping("/{id}")
    public Result<AppointmentVO> getAppointment(@PathVariable String id) {
        AppointmentVO vo = appointmentService.getAppointment(id);
        return Result.success(vo);
    }

    /**
     * 确认预约
     * PUT /api/appointment/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmAppointment(@PathVariable String id, @RequestBody Map<String, String> params) {
        appointmentService.confirmAppointment(id, params.get("providerId"), params.get("appointmentTime"));
        return Result.success("预约确认成功");
    }

    /**
     * 分配预约
     * PUT /api/appointment/{id}/assign
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assignAppointment(@PathVariable String id, @RequestBody Map<String, String> params) {
        appointmentService.assignAppointment(id, params.get("providerId"));
        return Result.success("预约分配成功");
    }

    /**
     * 取消预约
     * PUT /api/appointment/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelAppointment(@PathVariable String id, @RequestBody Map<String, String> params) {
        appointmentService.cancelAppointment(id, params.get("reason"));
        return Result.success("预约取消成功");
    }

    /**
     * 作废预约
     * PUT /api/appointment/{id}/invalidate
     */
    @PutMapping("/{id}/invalidate")
    public Result<Void> invalidateAppointment(@PathVariable String id, @RequestBody Map<String, String> params) {
        appointmentService.invalidateAppointment(id, params.get("reason"));
        return Result.success("预约作废成功");
    }

    /**
     * 导入预约（Excel）
     * POST /api/appointment/import
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importAppointment(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = appointmentService.importAppointment(file);
        return Result.success(result);
    }

    /**
     * 下载预约导入模板
     * GET /api/appointment/template
     */
    @GetMapping("/template")
    public Result<String> downloadTemplate() {
        String templateUrl = appointmentService.getTemplateUrl();
        return Result.success(templateUrl);
    }

    /**
     * 获取预约统计
     * GET /api/appointment/statistics
     */
    @GetMapping("/statistics")
    public Result<AppointmentStatisticsVO> getStatistics(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        AppointmentStatisticsVO result = appointmentService.getStatistics(areaId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 根据老人手机号获取预约历史
     * GET /api/appointment/phone
     */
    @GetMapping("/phone")
    public Result<List<AppointmentVO>> getAppointmentsByPhone(@RequestParam String phone) {
        List<AppointmentVO> list = appointmentService.getAppointmentsByPhone(phone);
        return Result.success(list);
    }

    /**
     * 获取预约时间轴
     * GET /api/appointment/{id}/timeline
     */
    @GetMapping("/{id}/timeline")
    public Result<Object> getAppointmentTimeline(@PathVariable String id) {
        Object timeline = appointmentService.getAppointmentTimeline(id);
        return Result.success(timeline);
    }
}
