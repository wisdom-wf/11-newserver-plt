package com.elderlycare.controller.quality;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.dto.quality.InspectionDTO;
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
    private final OrderMapper orderMapper;

    /**
     * 获取质检列表
     * GET /api/quality-check/list
     */
    @GetMapping("/list")
    public Result<PageResult<QualityCheckVO>> getQualityCheckList(QualityCheckQueryDTO query) {
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
        PageResult<QualityCheckVO> result = qualityCheckService.getQualityCheckList(query);
        return Result.success(result);
    }

    /**
     * 获取质检详情
     * GET /api/quality-check/{id}
     * 隔离：PROVIDER/STAFF只能查属于自己的质检
     */
    @GetMapping("/{id}")
    public Result<QualityCheckVO> getQualityCheck(@PathVariable String id) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        String staffIdCtx = UserContext.getStaffId();

        QualityCheckVO vo = qualityCheckService.getQualityCheck(id);
        if (vo == null) {
            return Result.notFound("质检记录不存在");
        }

        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(vo.getProviderId())) {
            throw BusinessException.fail("无权查看其他服务商的质检记录");
        }
        if ("STAFF".equals(userType) && staffIdCtx != null && !staffIdCtx.equals(vo.getStaffId())) {
            throw BusinessException.fail("无权查看其他人员的质检记录");
        }
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
     * 隔离：PROVIDER只能为自己公司的订单创建质检
     */
    @PostMapping
    public Result<Void> createQualityCheck(@RequestBody QualityCheckVO vo) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            Order order = orderMapper.selectById(vo.getOrderId());
            if (order == null) {
                throw BusinessException.notFound("订单不存在");
            }
            if (!autoPid.equals(order.getProviderId())) {
                throw BusinessException.forbidden("无权为他方订单创建质检");
            }
        }
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
     * 执行质检（质检员提交质检结论）
     * PUT /api/quality-check/{id}/inspect
     * QUALIFIED → 日志→APPROVED，订单→COMPLETED
     * UNQUALIFIED/NEED_RECTIFY → 开启整改流程
     */
    @PutMapping("/{id}/inspect")
    public Result<Void> inspect(@PathVariable String id, @RequestBody InspectionDTO dto) {
        qualityCheckService.inspect(id, dto);
        return Result.success("质检执行完成");
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
