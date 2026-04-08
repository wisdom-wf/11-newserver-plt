package com.elderlycare.service.financial.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.DateUtil;
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

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "COMPLETED");
        if (StringUtils.isNotBlank(dto.getProviderId())) {
            wrapper.eq(Order::getProviderId, dto.getProviderId());
        }
        if (StringUtils.isNotBlank(dto.getStaffId())) {
            wrapper.eq(Order::getStaffId, dto.getStaffId());
        }
        if (dto.getSettlementPeriodStart() != null) {
            wrapper.ge(Order::getServiceDate, dto.getSettlementPeriodStart());
        }
        if (dto.getSettlementPeriodEnd() != null) {
            wrapper.le(Order::getServiceDate, dto.getSettlementPeriodEnd());
        }

        List<Order> orders = orderMapper.selectList(wrapper);

        vo.setTotalOrderCount(orders.size());
        BigDecimal totalServiceAmount = orders.stream()
                .map(Order::getEstimatedPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSubsidyAmount = orders.stream()
                .map(Order::getSubsidyAmount)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSelfPayAmount = orders.stream()
                .map(Order::getSelfPayAmount)
                .filter(p -> p != null)
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
        if (!"PENDING".equals(settlement.getStatus())) {
            throw BusinessException.fail("该结算单已确认或已取消，无法重复确认");
        }

        settlement.setStatus("CONFIRMED");
        settlement.setConfirmTime(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        settlement.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(settlement);
    }

    @Override
    public PageResult<SettlementVO> querySettlements(SettlementQueryDTO dto) {
        Page<Settlement> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<Settlement> result = baseMapper.selectSettlementPage(page, dto);

        List<Settlement> records = result.getRecords();
        long total = result.getTotal();

        List<SettlementVO> voList = records.stream().map(this::convertToVO).collect(Collectors.toList());

        return PageResult.of(total, dto.getPage(), dto.getPageSize(), voList);
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

        if ("STAFF".equals(dto.getSettlementType()) && StringUtils.isNotBlank(dto.getStaffId())) {
            SettlementCalculateDTO calculateDTO = new SettlementCalculateDTO();
            BeanUtils.copyProperties(dto, calculateDTO);
            SettlementCalculateVO calculateResult = calculateSettlement(calculateDTO);

            Settlement settlement = new Settlement();
            settlement.setSettlementId(IDGenerator.generateId());
            settlement.setSettlementNo(generateSettlementNo());
            settlement.setSettlementType(dto.getSettlementType());
            settlement.setProviderId(dto.getProviderId());
            settlement.setStaffId(dto.getStaffId());
            settlement.setStaffName(calculateResult.getStaffName());
            settlement.setSettlementPeriodStart(dto.getSettlementPeriodStart());
            settlement.setSettlementPeriodEnd(dto.getSettlementPeriodEnd());
            settlement.setTotalOrderCount(calculateResult.getTotalOrderCount());
            settlement.setTotalServiceAmount(calculateResult.getTotalServiceAmount());
            settlement.setTotalSubsidyAmount(calculateResult.getTotalSubsidyAmount());
            settlement.setTotalSelfPayAmount(calculateResult.getTotalSelfPayAmount());
            settlement.setSettlementAmount(calculateResult.getCalculatedSettlementAmount());
            settlement.setStatus("PENDING");
            settlement.setCreateTime(LocalDateTime.now());
            settlement.setUpdateTime(LocalDateTime.now());

            baseMapper.insert(settlement);
            settlementIds.add(settlement.getSettlementId());
        } else if ("PROVIDER".equals(dto.getSettlementType()) && StringUtils.isNotBlank(dto.getProviderId())) {
            SettlementCalculateDTO calculateDTO = new SettlementCalculateDTO();
            BeanUtils.copyProperties(dto, calculateDTO);
            SettlementCalculateVO calculateResult = calculateSettlement(calculateDTO);

            Settlement settlement = new Settlement();
            settlement.setSettlementId(IDGenerator.generateId());
            settlement.setSettlementNo(generateSettlementNo());
            settlement.setSettlementType(dto.getSettlementType());
            settlement.setProviderId(dto.getProviderId());
            settlement.setSettlementPeriodStart(dto.getSettlementPeriodStart());
            settlement.setSettlementPeriodEnd(dto.getSettlementPeriodEnd());
            settlement.setTotalOrderCount(calculateResult.getTotalOrderCount());
            settlement.setTotalServiceAmount(calculateResult.getTotalServiceAmount());
            settlement.setTotalSubsidyAmount(calculateResult.getTotalSubsidyAmount());
            settlement.setTotalSelfPayAmount(calculateResult.getTotalSelfPayAmount());
            settlement.setSettlementAmount(calculateResult.getCalculatedSettlementAmount());
            settlement.setStatus("PENDING");
            settlement.setCreateTime(LocalDateTime.now());
            settlement.setUpdateTime(LocalDateTime.now());

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
        vo.setSettlementTypeName(getSettlementTypeName(settlement.getSettlementType()));
        vo.setStatusName(getStatusName(settlement.getStatus()));
        return vo;
    }

    private String getSettlementTypeName(String type) {
        if ("STAFF".equals(type)) {
            return "服务人员结算";
        } else if ("PROVIDER".equals(type)) {
            return "服务商结算";
        }
        return type;
    }

    private String getStatusName(String status) {
        if ("PENDING".equals(status)) {
            return "待确认";
        } else if ("CONFIRMED".equals(status)) {
            return "已确认";
        } else if ("CANCELLED".equals(status)) {
            return "已取消";
        } else if ("PAID".equals(status)) {
            return "已支付";
        }
        return status;
    }
}
