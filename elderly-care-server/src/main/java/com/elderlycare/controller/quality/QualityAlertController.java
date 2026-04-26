package com.elderlycare.controller.quality;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.quality.QualityAlert;
import com.elderlycare.service.quality.QualityAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量预警Controller
 */
@RestController
@RequestMapping("/api/quality-alert")
@RequiredArgsConstructor
public class QualityAlertController {

    private final QualityAlertService qualityAlertService;

    /**
     * 获取预警列表
     */
    @GetMapping("/list")
    public Result<PageResult<QualityAlert>> getAlertList(
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String alertStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 数据权限：服务商管理员只能查看自己的预警
        String userType = UserContext.getUserType();
        String providerId = null;
        if ("PROVIDER".equals(userType)) {
            providerId = UserContext.getProviderId();
        }
        PageResult<QualityAlert> result = qualityAlertService.getAlertList(providerId, staffId, alertType, alertStatus, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取预警详情
     */
    @GetMapping("/{alertId}")
    public Result<QualityAlert> getAlertById(@PathVariable String alertId) {
        QualityAlert alert = qualityAlertService.getAlertById(alertId);
        return Result.success(alert);
    }

    /**
     * 处理预警
     */
    @PutMapping("/{alertId}/handle")
    public Result<Void> handleAlert(@PathVariable String alertId, @RequestBody String handleResult) {
        qualityAlertService.handleAlert(alertId, handleResult);
        return Result.success("处理成功");
    }

    /**
     * 忽略预警
     */
    @PutMapping("/{alertId}/ignore")
    public Result<Void> ignoreAlert(@PathVariable String alertId) {
        qualityAlertService.ignoreAlert(alertId);
        return Result.success("已忽略");
    }

    /**
     * 触发预警检查
     */
    @PostMapping("/check")
    public Result<Void> checkAndGenerateAlerts() {
        qualityAlertService.checkAndGenerateAlerts();
        return Result.success("检查完成");
    }
}
