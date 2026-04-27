package com.elderlycare.controller.dispute;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.dispute.ServiceDispute;
import com.elderlycare.service.dispute.ServiceDisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务争议Controller
 */
@RestController
@RequestMapping("/api/service-dispute")
@RequiredArgsConstructor
public class ServiceDisputeController {

    private final ServiceDisputeService disputeService;

    /**
     * 创建争议申请
     */
    @PostMapping("")
    public Result<String> createDispute(@RequestBody ServiceDispute dispute) {
        String disputeId = disputeService.createDispute(dispute);
        return Result.<String>success(disputeId);
    }

    /**
     * 获取争议列表
     */
    @GetMapping("/list")
    public Result<PageResult<ServiceDispute>> getDisputeList(
            @RequestParam(required = false) String disputeType,
            @RequestParam(required = false) String disputeStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 数据权限：服务商管理员只能查看自己的争议
        String userType = UserContext.getUserType();
        String providerId = null;
        if ("PROVIDER".equals(userType)) {
            providerId = UserContext.getProviderId();
        }
        PageResult<ServiceDispute> result = disputeService.getDisputeList(providerId, disputeType, disputeStatus, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取争议详情
     */
    @GetMapping("/{disputeId}")
    public Result<ServiceDispute> getDisputeById(@PathVariable String disputeId) {
        ServiceDispute dispute = disputeService.getDisputeById(disputeId);
        return Result.success(dispute);
    }

    /**
     * 开始调查
     */
    @PutMapping("/{disputeId}/investigate")
    public Result<Void> startInvestigation(@PathVariable String disputeId, @RequestBody String investigationContent) {
        disputeService.startInvestigation(disputeId, investigationContent);
        return Result.successMsg("调查已开始");
    }

    /**
     * 进行调解
     */
    @PutMapping("/{disputeId}/mediate")
    public Result<Void> mediate(@PathVariable String disputeId, @RequestBody String mediationContent) {
        disputeService.mediate(disputeId, mediationContent);
        return Result.successMsg("调解已开始");
    }

    /**
     * 达成协议
     */
    @PutMapping("/{disputeId}/agree")
    public Result<Void> reachAgreement(@PathVariable String disputeId, @RequestBody String agreementContent) {
        disputeService.reachAgreement(disputeId, agreementContent);
        return Result.successMsg("协议已达成");
    }

    /**
     * 关闭争议
     */
    @PutMapping("/{disputeId}/close")
    public Result<Void> closeDispute(@PathVariable String disputeId, @RequestBody String closeReason) {
        disputeService.closeDispute(disputeId, closeReason);
        return Result.successMsg("争议已关闭");
    }
}
