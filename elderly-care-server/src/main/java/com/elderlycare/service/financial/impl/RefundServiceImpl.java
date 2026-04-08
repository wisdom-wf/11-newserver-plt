package com.elderlycare.service.financial.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.RefundAuditDTO;
import com.elderlycare.dto.financial.RefundCreateDTO;
import com.elderlycare.dto.financial.RefundQueryDTO;
import com.elderlycare.entity.financial.Refund;
import com.elderlycare.mapper.financial.RefundMapper;
import com.elderlycare.service.financial.RefundService;
import com.elderlycare.vo.financial.RefundVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款Service实现
 */
@Service
@RequiredArgsConstructor
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRefund(RefundCreateDTO dto) {
        Refund refund = new Refund();
        BeanUtils.copyProperties(dto, refund);
        refund.setRefundId(IDGenerator.generateId());
        refund.setRefundNo(generateRefundNo());
        refund.setAuditStatus("PENDING");
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(refund);
        return refund.getRefundId();
    }

    @Override
    public PageResult<RefundVO> queryRefunds(RefundQueryDTO dto) {
        Page<Refund> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<Refund> result = baseMapper.selectRefundPage(page, dto);

        List<Refund> records = result.getRecords();
        long total = result.getTotal();

        List<RefundVO> voList = records.stream().map(this::convertToVO).collect(Collectors.toList());

        return PageResult.of(total, dto.getPage(), dto.getPageSize(), voList);
    }

    @Override
    public RefundVO getRefundById(String refundId) {
        Refund refund = baseMapper.selectById(refundId);
        if (refund == null) {
            throw BusinessException.notFound("退款记录不存在");
        }
        return convertToVO(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(String refundId, RefundAuditDTO dto) {
        Refund refund = baseMapper.selectById(refundId);
        if (refund == null) {
            throw BusinessException.notFound("退款记录不存在");
        }
        if (!"PENDING".equals(refund.getAuditStatus())) {
            throw BusinessException.fail("该退款记录已审核，无法重复审核");
        }

        refund.setAuditStatus(dto.getAuditStatus());
        refund.setAuditComment(dto.getAuditComment());
        refund.setAuditTime(LocalDateTime.now());
        if (StringUtils.isNotBlank(dto.getRemark())) {
            refund.setRemark(dto.getRemark());
        }
        refund.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(refund);
    }

    private String generateRefundNo() {
        return "REF" + System.currentTimeMillis();
    }

    private RefundVO convertToVO(Refund refund) {
        RefundVO vo = new RefundVO();
        BeanUtils.copyProperties(refund, vo);
        vo.setAuditStatusName(getAuditStatusName(refund.getAuditStatus()));
        return vo;
    }

    private String getAuditStatusName(String status) {
        if ("PENDING".equals(status)) {
            return "待审核";
        } else if ("APPROVED".equals(status)) {
            return "已通过";
        } else if ("REJECTED".equals(status)) {
            return "已拒绝";
        } else if ("COMPLETED".equals(status)) {
            return "已完成";
        }
        return status;
    }
}
