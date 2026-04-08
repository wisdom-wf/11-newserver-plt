package com.elderlycare.service.financial.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.FinancialReportQueryDTO;
import com.elderlycare.entity.financial.Refund;
import com.elderlycare.entity.financial.Settlement;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.financial.RefundMapper;
import com.elderlycare.mapper.financial.SettlementMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.financial.FinancialReportService;
import com.elderlycare.vo.financial.FinancialReportVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务报表Service实现
 */
@Service
@RequiredArgsConstructor
public class FinancialReportServiceImpl implements FinancialReportService {

    private final OrderMapper orderMapper;
    private final SettlementMapper settlementMapper;
    private final RefundMapper refundMapper;

    @Override
    public PageResult<FinancialReportVO> queryReports(FinancialReportQueryDTO dto) {
        List<FinancialReportVO> reportList = new ArrayList<>();

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        if (dto.getStartDate() != null) {
            orderWrapper.ge(Order::getServiceDate, dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            orderWrapper.le(Order::getServiceDate, dto.getEndDate());
        }
        if (StringUtils.isNotBlank(dto.getProviderId())) {
            orderWrapper.eq(Order::getProviderId, dto.getProviderId());
        }

        List<Order> orders = orderMapper.selectList(orderWrapper);

        FinancialReportVO report = new FinancialReportVO();
        report.setReportDate(LocalDate.now());
        report.setProviderId(dto.getProviderId());

        if (!orders.isEmpty()) {
            Order firstOrder = orders.get(0);
            report.setProviderId(firstOrder.getProviderId());
            // providerName需要通过providerId查询获取，此处简化处理

            long completedCount = orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
            long cancelledCount = orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

            report.setTotalOrderCount(orders.size());
            report.setCompletedOrderCount((int) completedCount);
            report.setCancelledOrderCount((int) cancelledCount);

            BigDecimal totalServiceAmount = orders.stream()
                    .map(Order::getEstimatedPrice)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalGovernmentSubsidy = orders.stream()
                    .map(Order::getSubsidyAmount)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSelfPay = orders.stream()
                    .map(Order::getSelfPayAmount)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            report.setTotalServiceAmount(totalServiceAmount);
            report.setTotalGovernmentSubsidy(totalGovernmentSubsidy);
            report.setTotalSelfPay(totalSelfPay);
        }

        LambdaQueryWrapper<Settlement> settlementWrapper = new LambdaQueryWrapper<>();
        if (dto.getStartDate() != null) {
            settlementWrapper.ge(Settlement::getCreateTime, dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            settlementWrapper.le(Settlement::getCreateTime, dto.getEndDate());
        }
        if (StringUtils.isNotBlank(dto.getProviderId())) {
            settlementWrapper.eq(Settlement::getProviderId, dto.getProviderId());
        }
        settlementWrapper.eq(Settlement::getStatus, "CONFIRMED");

        List<Settlement> settlements = settlementMapper.selectList(settlementWrapper);
        BigDecimal totalSettlementAmount = settlements.stream()
                .map(Settlement::getSettlementAmount)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalSettlementAmount(totalSettlementAmount);

        LambdaQueryWrapper<Refund> refundWrapper = new LambdaQueryWrapper<>();
        if (dto.getStartDate() != null) {
            refundWrapper.ge(Refund::getCreateTime, dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            refundWrapper.le(Refund::getCreateTime, dto.getEndDate());
        }
        if (StringUtils.isNotBlank(dto.getProviderId())) {
            refundWrapper.eq(Refund::getProviderId, dto.getProviderId());
        }
        refundWrapper.eq(Refund::getAuditStatus, "COMPLETED");

        List<Refund> refunds = refundMapper.selectList(refundWrapper);
        BigDecimal totalRefundAmount = refunds.stream()
                .map(Refund::getRefundAmount)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRefundAmount(totalRefundAmount);

        BigDecimal netAmount = report.getTotalServiceAmount()
                .subtract(report.getTotalRefundAmount() != null ? report.getTotalRefundAmount() : BigDecimal.ZERO);
        report.setNetAmount(netAmount);

        reportList.add(report);

        long total = reportList.size();
        return PageResult.of(total, 1, 10, reportList);
    }
}
