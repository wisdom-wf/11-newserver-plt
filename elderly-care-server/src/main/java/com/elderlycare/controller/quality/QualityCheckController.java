package com.elderlycare.controller.quality;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.quality.QualityCheckQueryDTO;
import com.elderlycare.service.quality.QualityCheckService;
import com.elderlycare.vo.quality.QualityCheckStatisticsVO;
import com.elderlycare.vo.quality.QualityCheckVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 质检控制器
 */
@RestController
@RequestMapping("/api/quality-check")
@RequiredArgsConstructor
public class QualityCheckController {

    private final QualityCheckService qualityCheckService;

    /**
     * 获取质检列表
     * GET /api/quality-check/list
     */
    @GetMapping("/list")
    public Result<PageResult<QualityCheckVO>> getQualityCheckList(QualityCheckQueryDTO query) {
        // 数据权限：服务商管理员自动注入 providerId
        String autoPid = UserContext.getProviderId();
        if (autoPid != null) {
            query.setProviderId(autoPid);
        }
        // STAFF角色：注入staffId（只能看自己的）
        String staffId = UserContext.getStaffId();
        if (staffId != null) {
            query.setStaffId(staffId);
        }
        PageResult<QualityCheckVO> result = qualityCheckService.getQualityCheckList(query);
        return Result.success(result);
    }

    /**
     * 获取质检详情
     * GET /api/quality-check/{id}
     */
    @GetMapping("/{id}")
    public Result<QualityCheckVO> getQualityCheck(@PathVariable String id) {
        QualityCheckVO vo = qualityCheckService.getQualityCheck(id);
        return Result.success(vo);
    }

    /**
     * 根据订单ID获取质检
     * GET /api/quality-check/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Result<QualityCheckVO> getQualityCheckByOrderId(@PathVariable String orderId) {
        QualityCheckVO vo = qualityCheckService.getQualityCheckByOrderId(orderId);
        return Result.success(vo);
    }

    /**
     * 创建质检
     * POST /api/quality-check
     */
    @PostMapping
    public Result<Void> createQualityCheck(@RequestBody QualityCheckVO vo) {
        qualityCheckService.createQualityCheck(vo);
        return Result.success("质检创建成功");
    }

    /**
     * 更新质检
     * PUT /api/quality-check/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateQualityCheck(@PathVariable String id, @RequestBody QualityCheckVO vo) {
        qualityCheckService.updateQualityCheck(id, vo);
        return Result.success("质检更新成功");
    }

    /**
     * 提交整改
     * PUT /api/quality-check/{id}/rectify
     */
    @PutMapping("/{id}/rectify")
    public Result<Void> submitRectify(@PathVariable String id, @RequestBody Map<String, Object> params) {
        qualityCheckService.submitRectify(id, params);
        return Result.success("整改提交成功");
    }

    /**
     * 复检
     * PUT /api/quality-check/{id}/recheck
     */
    @PutMapping("/{id}/recheck")
    public Result<Void> recheck(@PathVariable String id, @RequestBody Map<String, Object> params) {
        qualityCheckService.recheck(id, params);
        return Result.success("复检成功");
    }

    /**
     * 获取质检统计
     * GET /api/quality-check/statistics
     */
    @GetMapping("/statistics")
    public Result<QualityCheckStatisticsVO> getStatistics(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        QualityCheckStatisticsVO result = qualityCheckService.getStatistics(areaId, providerId, startDate, endDate);
        return Result.success(result);
    }
}
