package com.elderlycare.controller.servicelog;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.servicelog.DepartureDTO;
import com.elderlycare.dto.servicelog.ServiceLogQueryDTO;
import com.elderlycare.dto.servicelog.SignInDTO;
import com.elderlycare.dto.servicelog.SignOutDTO;
import com.elderlycare.service.servicelog.ServiceLogService;
import com.elderlycare.vo.servicelog.ServiceLogStatisticsVO;
import com.elderlycare.vo.servicelog.ServiceLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务日志控制器
 */
@RestController
@RequestMapping("/api/service-log")
@RequiredArgsConstructor
public class ServiceLogController {

    private final ServiceLogService serviceLogService;

    /**
     * 获取服务日志列表
     * GET /api/service-log/list
     */
    @GetMapping("/list")
    public Result<PageResult<ServiceLogVO>> getServiceLogList(ServiceLogQueryDTO query) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            query.setProviderId(autoPid);
            query.setStaffId(null);
        }
        String staffId = UserContext.getStaffId();
        if ("STAFF".equals(userType) && staffId != null) {
            query.setStaffId(staffId);
            query.setProviderId(null);
        }
        PageResult<ServiceLogVO> result = serviceLogService.getServiceLogList(query);
        return Result.success(result);
    }

    /**
     * 获取服务日志详情
     * GET /api/service-log/{id}
     * 隔离：PROVIDER/STAFF只能查属于自己的日志
     */
    @GetMapping("/{id}")
    public Result<ServiceLogVO> getServiceLog(@PathVariable String id) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        String staffIdCtx = UserContext.getStaffId();

        ServiceLogVO vo = serviceLogService.getServiceLog(id);
        if (vo == null) {
            return Result.notFound("服务日志不存在");
        }

        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(vo.getProviderId())) {
            throw BusinessException.fail("无权查看其他服务商的日志");
        }
        if ("STAFF".equals(userType) && staffIdCtx != null && !staffIdCtx.equals(vo.getStaffId())) {
            throw BusinessException.fail("无权查看其他人员的服务日志");
        }
        return Result.success(vo);
    }

    /**
     * 根据订单ID获取服务日志
     * GET /api/service-log/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Result<ServiceLogVO> getServiceLogByOrderId(@PathVariable String orderId) {
        ServiceLogVO vo = serviceLogService.getServiceLogByOrderId(orderId);
        if (vo == null) {
            return Result.notFound("该订单暂无服务日志");
        }
        return Result.success(vo);
    }

    /**
     * 提交服务日志
     * POST /api/service-log
     */
    @PostMapping
    public Result<String> submitServiceLog(@RequestBody ServiceLogVO vo) {
        String serviceLogId = serviceLogService.submitServiceLog(vo);
        return Result.success(serviceLogId);
    }

    /**
     * 更新服务日志
     * PUT /api/service-log/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateServiceLog(@PathVariable String id, @RequestBody ServiceLogVO vo) {
        serviceLogService.updateServiceLog(id, vo);
        return Result.success("服务日志更新成功");
    }

    /**
     * 上报异常
     * PUT /api/service-log/{id}/anomaly
     */
    @PutMapping("/{id}/anomaly")
    public Result<Void> reportAnomaly(@PathVariable String id, @RequestBody Map<String, Object> params) {
        serviceLogService.reportAnomaly(id, params);
        return Result.success("异常上报成功");
    }

    /**
     * 获取服务日志统计
     * GET /api/service-log/statistics
     */
    @GetMapping("/statistics")
    public Result<ServiceLogStatisticsVO> getStatistics(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        ServiceLogStatisticsVO result = serviceLogService.getStatistics(areaId, providerId, staffId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 提交服务日志审核
     * PUT /api/service-log/{id}/submit-review
     */
    @PutMapping("/{id}/submit-review")
    public Result<Void> submitForReview(@PathVariable String id, @RequestParam(required = false) String remarks) {
        serviceLogService.submitForReview(id, remarks);
        return Result.success("提交审核成功");
    }

    /**
     * 审核服务日志
     * PUT /api/service-log/{id}/review
     */
    @PutMapping("/{id}/review")
    public Result<Void> reviewServiceLog(
            @PathVariable String id,
            @RequestParam String result,
            @RequestParam(required = false) String reviewComment) {
        serviceLogService.reviewServiceLog(id, result, reviewComment);
        return Result.success("审核完成");
    }

    /**
     * 删除服务日志
     * DELETE /api/service-log/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteServiceLog(@PathVariable String id) {
        serviceLogService.deleteServiceLog(id);
        return Result.success("删除成功");
    }

    /**
     * 服务日志批量删除
     * DELETE /api/service-log/batch
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteServiceLog(@RequestBody List<String> ids) {
        for (String id : ids) {
            serviceLogService.deleteServiceLog(id);
        }
        return Result.success("批量删除成功");
    }

    /**
     * 出发
     * PUT /api/service-log/{id}/departure
     */
    @PutMapping("/{id}/departure")
    public Result<Void> departure(@PathVariable String id, @RequestBody DepartureDTO dto) {
        serviceLogService.departure(id, dto);
        return Result.success("出发登记成功");
    }

    /**
     * 签到
     * PUT /api/service-log/{id}/sign-in
     */
    @PutMapping("/{id}/sign-in")
    public Result<Void> signIn(@PathVariable String id, @RequestBody SignInDTO dto) {
        serviceLogService.signIn(id, dto);
        return Result.success("签到成功");
    }

    /**
     * 签退
     * PUT /api/service-log/{id}/sign-out
     */
    @PutMapping("/{id}/sign-out")
    public Result<Void> signOut(@PathVariable String id, @RequestBody SignOutDTO dto) {
        serviceLogService.signOut(id, dto);
        return Result.success("签退成功");
    }
}
