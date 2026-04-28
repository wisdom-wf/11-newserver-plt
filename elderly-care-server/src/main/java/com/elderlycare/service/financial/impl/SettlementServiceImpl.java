package com.elderlycare.service.financial.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.BatchSettlementDTO;
import com.elderlycare.dto.financial.SettlementCalculateDTO;
import com.elderlycare.dto.financial.SettlementQueryDTO;
import com.elderlycare.entity.financial.Settlement;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.financial.SettlementMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.financial.SettlementService;
import com.elderlycare.vo.financial.SettlementCalculateVO;
import com.elderlycare.vo.financial.SettlementVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 结算Service实现
 */
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl extends ServiceImpl<SettlementMapper, Settlement> implements SettlementService {

    private final OrderMapper orderMapper;

    @Override
    public SettlementCalculateVO calculateSettlement(SettlementCalculateDTO dto) {
        SettlementCalculateVO vo = new SettlementCalculateVO();
        vo.setProviderId(dto.getProviderId());
        vo.setStaffId(dto.getStaffId());
        vo.setSettlementPeriodStart(dto.getSettlementPeriodStart());
        vo.setSettlementPeriodEnd(dto.getSettlementPeriodEnd());

        LambdaQueryWrapper<Settlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Settlement::getPaymentStatus, "UNPAID")
               .or().eq(Settlement::getPaymentStatus, "PENDING");
        List<Settlement> existing = baseMapper.selectList(wrapper);
        List<String> settledOrderIds = existing.stream()
                .map(Settlement::getOrderId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getStatus, "COMPLETED");
        if (StringUtils.isNotBlank(dto.getProviderId())) {
            orderWrapper.eq(Order::getProviderId, dto.getProviderId());
        }
        if (StringUtils.isNotBlank(dto.getStaffId())) {
            orderWrapper.eq(Order::getStaffId, dto.getStaffId());
        }
        if (dto.getSettlementPeriodStart() != null) {
            orderWrapper.ge(Order::getServiceDate, dto.getSettlementPeriodStart());
        }
        if (dto.getSettlementPeriodEnd() != null) {
            orderWrapper.le(Order::getServiceDate, dto.getSettlementPeriodEnd());
        }

        List<Order> orders = orderMapper.selectList(orderWrapper);
        List<Order> billable = orders.stream()
                .filter(o -> !settledOrderIds.contains(o.getOrderId()))
                .collect(Collectors.toList());

        vo.setTotalOrderCount(billable.size());
        BigDecimal totalServiceAmount = billable.stream()
                .map(Order::getEstimatedPrice).filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSubsidyAmount = billable.stream()
                .map(Order::getSubsidyAmount).filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSelfPayAmount = billable.stream()
                .map(Order::getSelfPayAmount).filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        vo.setTotalServiceAmount(totalServiceAmount);
        vo.setTotalSubsidyAmount(totalSubsidyAmount);
        vo.setTotalSelfPayAmount(totalSelfPayAmount);
        vo.setCalculatedSettlementAmount(totalSubsidyAmount);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmSettlement(String settlementId) {
        Settlement settlement = baseMapper.selectById(settlementId);
        if (settlement == null) {
            throw BusinessException.notFound("结算单不存在");
        }
        String status = settlement.getPaymentStatus();
        if (!"UNPAID".equals(status) && !"PENDING".equals(status)) {
            throw BusinessException.fail("该结算单已确认或已取消，无法重复确认");
        }
        settlement.setPaymentStatus("CONFIRMED");
        settlement.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(settlement);
    }

    @Override
    public PageResult<SettlementVO> querySettlements(SettlementQueryDTO dto) {
        Page<Settlement> page = new Page<>(dto.getPage(), dto.getPageSize());
        @SuppressWarnings("unchecked")
        IPage<Settlement> result = ((SettlementMapper) baseMapper).selectSettlementPage(page, dto);
        List<SettlementVO> voList = result.getRecords().stream()
                .map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), dto.getPage(), dto.getPageSize(), voList);
    }

    @Override
    public SettlementVO getSettlementById(String settlementId) {
        Settlement settlement = baseMapper.selectById(settlementId);
        if (settlement == null) {
            throw BusinessException.notFound("结算单不存在");
        }
        return convertToVO(settlement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> batchSettlement(BatchSettlementDTO dto) {
        List<String> settlementIds = new ArrayList<>();

        LambdaQueryWrapper<Settlement> settleWrapper = new LambdaQueryWrapper<>();
        settleWrapper.select(Settlement::getOrderId)
                .isNotNull(Settlement::getOrderId);
        List<String> existingOrderIds = baseMapper.selectList(settleWrapper).stream()
                .map(Settlement::getOrderId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getStatus, "COMPLETED");
        if (StringUtils.isNotBlank(dto.getProviderId())) {
            orderWrapper.eq(Order::getProviderId, dto.getProviderId());
        }
        if (StringUtils.isNotBlank(dto.getStaffId())) {
            orderWrapper.eq(Order::getStaffId, dto.getStaffId());
        }
        if (dto.getSettlementPeriodStart() != null) {
            orderWrapper.ge(Order::getServiceDate, dto.getSettlementPeriodStart());
        }
        if (dto.getSettlementPeriodEnd() != null) {
            orderWrapper.le(Order::getServiceDate, dto.getSettlementPeriodEnd());
        }

        List<Order> orders = orderMapper.selectList(orderWrapper);
        if (orders.isEmpty()) {
            return settlementIds;
        }

        for (Order order : orders) {
            if (existingOrderIds.contains(order.getOrderId())) {
                continue;
            }
            Settlement settlement = new Settlement();
            settlement.setSettlementId(IDGenerator.generateId());
            settlement.setSettlementNo(generateSettlementNo());
            settlement.setProviderId(order.getProviderId());
            settlement.setStaffId(order.getStaffId());
            settlement.setOrderId(order.getOrderId());
            settlement.setServiceDate(order.getServiceDate());
            settlement.setElderId(order.getElderId());
            settlement.setTotalServiceAmount(order.getEstimatedPrice());
            settlement.setTotalSubsidyAmount(order.getSubsidyAmount());
            settlement.setTotalSelfPayAmount(order.getSelfPayAmount());
            settlement.setUnitPrice(order.getEstimatedPrice());
            settlement.setPaymentStatus("UNPAID");
            settlement.setCreateTime(LocalDateTime.now());
            baseMapper.insert(settlement);
            settlementIds.add(settlement.getSettlementId());
        }
        return settlementIds;
    }

    private String generateSettlementNo() {
        return "SET" + System.currentTimeMillis();
    }

    private SettlementVO convertToVO(Settlement settlement) {
        SettlementVO vo = new SettlementVO();
        BeanUtils.copyProperties(settlement, vo);
        vo.setStatus(settlement.getPaymentStatus());
        vo.setStatusName(getStatusName(settlement.getPaymentStatus()));
        vo.setElderName(settlement.getElderName());
        return vo;
    }

    private String getStatusName(String status) {
        if ("PENDING".equals(status) || "UNPAID".equals(status)) return "待结算";
        if ("CONFIRMED".equals(status) || "SETTLED".equals(status)) return "已结算";
        if ("PAID".equals(status)) return "已支付";
        if ("CANCELLED".equals(status)) return "已取消";
        return status;
    }
}
