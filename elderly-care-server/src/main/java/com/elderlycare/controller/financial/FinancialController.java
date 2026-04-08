package com.elderlycare.controller.financial;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.financial.*;
import com.elderlycare.service.financial.*;
import com.elderlycare.vo.financial.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 财务结算管理Controller
 */
@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final ServicePriceService servicePriceService;
    private final SettlementService settlementService;
    private final RefundService refundService;
    private final FinancialReportService financialReportService;

    // ==================== 服务定价管理接口 ====================

    /**
     * 定价新增
     */
    @PostMapping("/prices")
    public Result<String> createPrice(@Validated @RequestBody ServicePriceCreateDTO dto) {
        String priceId = servicePriceService.createPrice(dto);
        return Result.success(priceId);
    }

    /**
     * 定价列表
     */
    @GetMapping("/prices")
    public Result<PageResult<ServicePriceVO>> queryPrices(ServicePriceQueryDTO dto) {
        PageResult<ServicePriceVO> result = servicePriceService.queryPrices(dto);
        return Result.success(result);
    }

    /**
     * 定价详情
     */
    @GetMapping("/prices/{priceId}")
    public Result<ServicePriceVO> getPriceById(@PathVariable String priceId) {
        ServicePriceVO vo = servicePriceService.getPriceById(priceId);
        return Result.success(vo);
    }

    /**
     * 定价修改
     */
    @PutMapping("/prices/{priceId}")
    public Result<Void> updatePrice(
            @PathVariable String priceId,
            @Validated @RequestBody ServicePriceUpdateDTO dto) {
        servicePriceService.updatePrice(priceId, dto);
        return Result.success();
    }

    /**
     * 定价删除
     */
    @DeleteMapping("/prices/{priceId}")
    public Result<Void> deletePrice(@PathVariable String priceId) {
        servicePriceService.deletePrice(priceId);
        return Result.success();
    }

    // ==================== 结算管理接口 ====================

    /**
     * 结算计算
     */
    @PostMapping("/settlements/calculate")
    public Result<SettlementCalculateVO> calculateSettlement(@Validated @RequestBody SettlementCalculateDTO dto) {
        SettlementCalculateVO result = settlementService.calculateSettlement(dto);
        return Result.success(result);
    }

    /**
     * 结算确认
     */
    @PostMapping("/settlements/{settlementId}/confirm")
    public Result<Void> confirmSettlement(@PathVariable String settlementId) {
        settlementService.confirmSettlement(settlementId);
        return Result.success();
    }

    /**
     * 结算列表
     */
    @GetMapping("/settlements")
    public Result<PageResult<SettlementVO>> querySettlements(SettlementQueryDTO dto) {
        PageResult<SettlementVO> result = settlementService.querySettlements(dto);
        return Result.success(result);
    }

    /**
     * 结算详情
     */
    @GetMapping("/settlements/{settlementId}")
    public Result<SettlementVO> getSettlementById(@PathVariable String settlementId) {
        SettlementVO vo = settlementService.getSettlementById(settlementId);
        return Result.success(vo);
    }

    /**
     * 批量结算
     */
    @PostMapping("/settlements/batch")
    public Result<List<String>> batchSettlement(@Validated @RequestBody BatchSettlementDTO dto) {
        List<String> settlementIds = settlementService.batchSettlement(dto);
        return Result.success(settlementIds);
    }

    // ==================== 退款管理接口 ====================

    /**
     * 退款申请
     */
    @PostMapping("/refunds")
    public Result<String> createRefund(@Validated @RequestBody RefundCreateDTO dto) {
        String refundId = refundService.createRefund(dto);
        return Result.success(refundId);
    }

    /**
     * 退款列表
     */
    @GetMapping("/refunds")
    public Result<PageResult<RefundVO>> queryRefunds(RefundQueryDTO dto) {
        PageResult<RefundVO> result = refundService.queryRefunds(dto);
        return Result.success(result);
    }

    /**
     * 退款详情
     */
    @GetMapping("/refunds/{refundId}")
    public Result<RefundVO> getRefundById(@PathVariable String refundId) {
        RefundVO vo = refundService.getRefundById(refundId);
        return Result.success(vo);
    }

    /**
     * 退款审核
     */
    @PutMapping("/refunds/{refundId}/audit")
    public Result<Void> auditRefund(
            @PathVariable String refundId,
            @Validated @RequestBody RefundAuditDTO dto) {
        refundService.auditRefund(refundId, dto);
        return Result.success();
    }

    // ==================== 财务报表接口 ====================

    /**
     * 财务报表查询
     */
    @GetMapping("/reports")
    public Result<PageResult<FinancialReportVO>> queryReports(FinancialReportQueryDTO dto) {
        PageResult<FinancialReportVO> result = financialReportService.queryReports(dto);
        return Result.success(result);
    }
}
