package com.elderlycare.service.quality.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.quality.InspectionDTO;
import com.elderlycare.dto.quality.QualityCheckQueryDTO;
import com.elderlycare.entity.quality.QualityCheck;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.quality.QualityCheckService;
import com.elderlycare.vo.quality.QualityCheckStatisticsVO;
import com.elderlycare.vo.quality.QualityCheckVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 质检服务实现
 */
@Service
@RequiredArgsConstructor
public class QualityCheckServiceImpl implements QualityCheckService {

    private final QualityCheckMapper qualityCheckMapper;
    private final ServiceLogMapper serviceLogMapper;
    private final OrderMapper orderMapper;

    @Override
    public PageResult<QualityCheckVO> getQualityCheckList(QualityCheckQueryDTO query) {
        Page<QualityCheck> page = new Page<>(query.getCurrent(), query.getPageSize());
        LambdaQueryWrapper<QualityCheck> wrapper = new LambdaQueryWrapper<>();

        if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
            wrapper.eq(QualityCheck::getOrderNo, query.getOrderNo());
        }
        if (query.getProviderName() != null && !query.getProviderName().isEmpty()) {
            wrapper.like(QualityCheck::getProviderName, query.getProviderName());
        }
        if (query.getStaffName() != null && !query.getStaffName().isEmpty()) {
            wrapper.like(QualityCheck::getStaffName, query.getStaffName());
        }
        if (query.getCheckType() != null && !query.getCheckType().isEmpty()) {
            wrapper.eq(QualityCheck::getCheckType, query.getCheckType());
        }
        if (query.getCheckResult() != null && !query.getCheckResult().isEmpty()) {
            wrapper.eq(QualityCheck::getCheckResult, query.getCheckResult());
        }
        if (query.getProviderId() != null && !query.getProviderId().isEmpty()) {
            wrapper.eq(QualityCheck::getProviderId, query.getProviderId());
        }
        // 日期范围过滤 - 使用 checkTime 字段
        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            wrapper.ge(QualityCheck::getCheckTime, query.getStartDate() + " 00:00:00");
        }
        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            wrapper.le(QualityCheck::getCheckTime, query.getEndDate() + " 23:59:59");
        }

        wrapper.orderByDesc(QualityCheck::getCreateTime);

        IPage<QualityCheck> result = qualityCheckMapper.selectPage(page, wrapper);

        return new PageResult<>(
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList())
        );
    }

    @Override
    public QualityCheckVO getQualityCheck(String id) {
        QualityCheck qualityCheck = qualityCheckMapper.selectById(id);
        return convertToVO(qualityCheck);
    }

    @Override
    public QualityCheckVO getQualityCheckByOrderId(String orderId) {
        LambdaQueryWrapper<QualityCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QualityCheck::getOrderId, orderId);
        wrapper.orderByDesc(QualityCheck::getCreateTime);
        wrapper.last("LIMIT 1");
        QualityCheck qualityCheck = qualityCheckMapper.selectOne(wrapper);
        return convertToVO(qualityCheck);
    }

    @Override
    public QualityCheckVO getQualityCheckByServiceLogId(String serviceLogId) {
        LambdaQueryWrapper<QualityCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QualityCheck::getServiceLogId, serviceLogId);
        wrapper.orderByDesc(QualityCheck::getCreateTime);
        wrapper.last("LIMIT 1");
        QualityCheck qualityCheck = qualityCheckMapper.selectOne(wrapper);
        return convertToVO(qualityCheck);
    }

    @Override
    public void createQualityCheck(QualityCheckVO vo) {
        QualityCheck qualityCheck = new QualityCheck();
        qualityCheck.setCheckNo("QC" + System.currentTimeMillis());
        qualityCheck.setOrderId(vo.getOrderId());
        qualityCheck.setOrderNo(vo.getOrderNo());
        qualityCheck.setServiceLogId(vo.getServiceLogId());
        qualityCheck.setServiceCategory(vo.getServiceCategory());
        qualityCheck.setProviderId(vo.getProviderId());
        qualityCheck.setProviderName(vo.getProviderName());
        qualityCheck.setStaffId(vo.getStaffId());
        qualityCheck.setStaffName(vo.getStaffName());
        qualityCheck.setCheckType(vo.getCheckType());
        qualityCheck.setCheckMethod(vo.getCheckMethod());
        qualityCheck.setCheckScore(vo.getCheckScore());
        // 手动创建的质检单也默认 PENDING，统一由质检员通过 inspect 接口给出结论
        qualityCheck.setCheckResult(vo.getCheckResult() != null && !vo.getCheckResult().isEmpty()
                ? vo.getCheckResult() : "PENDING");
        qualityCheck.setCheckPhotos(vo.getCheckPhotos());
        qualityCheck.setCheckRemark(vo.getCheckRemark());
        qualityCheck.setCheckTime(LocalDateTime.now());
        qualityCheck.setNeedRectify(vo.getNeedRectify());
        qualityCheck.setRectifyStatus("PENDING");
        qualityCheckMapper.insert(qualityCheck);
    }

    @Override
    public void updateQualityCheck(String id, QualityCheckVO vo) {
        QualityCheck qualityCheck = qualityCheckMapper.selectById(id);
        if (qualityCheck != null) {
            qualityCheck.setCheckScore(vo.getCheckScore());
            qualityCheck.setCheckResult(vo.getCheckResult());
            qualityCheck.setCheckPhotos(vo.getCheckPhotos());
            qualityCheck.setCheckRemark(vo.getCheckRemark());
            qualityCheckMapper.updateById(qualityCheck);
        }
    }

    @Override
    public void submitRectify(String id, Map<String, Object> params) {
        QualityCheck qualityCheck = qualityCheckMapper.selectById(id);
        if (qualityCheck != null) {
            qualityCheck.setRectifyPhotos((String) params.get("photos"));
            qualityCheck.setRectifyRemark((String) params.get("remark"));
            qualityCheck.setRectifyStatus("RECHECK");
            qualityCheckMapper.updateById(qualityCheck);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recheck(String id, Map<String, Object> params) {
        QualityCheck qualityCheck = qualityCheckMapper.selectById(id);
        if (qualityCheck != null) {
            qualityCheck.setRecheckResult((String) params.get("result"));
            qualityCheck.setRecheckTime(LocalDateTime.now());
            if ("PASSED".equals(params.get("result"))) {
                qualityCheck.setRectifyStatus("VERIFIED");
                // 复检合格：联动日志→COMPLETED + 订单→COMPLETED
                if (qualityCheck.getServiceLogId() != null) {
                    ServiceLog sl = serviceLogMapper.selectById(qualityCheck.getServiceLogId());
                    if (sl != null) {
                        sl.setAuditStatus("COMPLETED");
                        serviceLogMapper.updateById(sl);
                    }
                }
                if (qualityCheck.getOrderId() != null) {
                    Order order = orderMapper.selectById(qualityCheck.getOrderId());
                    if (order != null) {
                        order.setStatus("COMPLETED");
                        order.setCompleteTime(LocalDateTime.now());
                        orderMapper.updateById(order);
                    }
                }
            } else {
                qualityCheck.setRectifyStatus("FAILED");
            }
            qualityCheckMapper.updateById(qualityCheck);
        }
    }

    @Override
    public QualityCheckStatisticsVO getStatistics(String areaId, String providerId, String startDate, String endDate) {
        LambdaQueryWrapper<QualityCheck> wrapper = new LambdaQueryWrapper<>();

        QualityCheckStatisticsVO stats = new QualityCheckStatisticsVO();

        // 总数
        Long total = qualityCheckMapper.selectCount(wrapper.clone());
        stats.setTotal(total.intValue());

        // 合格数
        wrapper.eq(QualityCheck::getCheckResult, "QUALIFIED");
        Long qualifiedCount = qualityCheckMapper.selectCount(wrapper.clone());
        stats.setQualifiedCount(qualifiedCount.intValue());
        wrapper.clear();

        // 不合格数
        wrapper.eq(QualityCheck::getCheckResult, "UNQUALIFIED");
        stats.setUnqualifiedCount(qualityCheckMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 需整改数
        wrapper.eq(QualityCheck::getCheckResult, "NEED_RECTIFY");
        stats.setNeedRectifyCount(qualityCheckMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 合格率
        if (total > 0) {
            BigDecimal rate = BigDecimal.valueOf(qualifiedCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, BigDecimal.ROUND_HALF_UP);
            stats.setQualifiedRate(rate);
        } else {
            stats.setQualifiedRate(BigDecimal.ZERO);
        }

        // 平均评分（无数据时返回null，前端formatter处理）
        stats.setAvgScore(qualityCheckMapper.avgCheckScore());  // null则保持null

        return stats;
    }

    /**
     * 执行质检（质检员提交质检结论）
     * QUALIFIED → 日志状态→APPROVED，订单状态→COMPLETED（联动完成）
     * UNQUALIFIED / NEED_RECTIFY → 开启整改流程（不联动日志/订单）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inspect(String id, InspectionDTO dto) {
        QualityCheck qc = qualityCheckMapper.selectById(id);
        if (qc == null) {
            throw new RuntimeException("质检单不存在");
        }
        if (!"PENDING".equals(qc.getCheckResult())) {
            throw new BusinessException(400, "只有待质检状态才能执行质检");
        }
        if (dto.getCheckResult() == null || dto.getCheckResult().isEmpty()) {
            throw new BusinessException(400, "质检结论不能为空");
        }
        // 质检结果只能是 QUALIFIED / UNQUALIFIED / NEED_RECTIFY
        if (!"QUALIFIED".equals(dto.getCheckResult())
                && !"UNQUALIFIED".equals(dto.getCheckResult())
                && !"NEED_RECTIFY".equals(dto.getCheckResult())) {
            throw new BusinessException(400, "质检结论必须是 QUALIFIED / UNQUALIFIED / NEED_RECTIFY");
        }

        // 填充质检信息
        qc.setCheckResult(dto.getCheckResult());
        qc.setCheckScore(dto.getCheckScore());
        qc.setCheckRemark(dto.getCheckRemark());
        qc.setCheckPhotos(dto.getCheckPhotos());
        qc.setCheckTime(LocalDateTime.now());
        qc.setCheckerId(UserContext.getUserId());
        qc.setCheckerName(UserContext.getUsername());

        if ("QUALIFIED".equals(dto.getCheckResult())) {
            // 合格：联动日志+订单状态
            if (qc.getServiceLogId() != null) {
                ServiceLog sl = serviceLogMapper.selectById(qc.getServiceLogId());
                if (sl != null) {
                    sl.setAuditStatus("APPROVED");
                    sl.setReviewTime(LocalDateTime.now());
                    sl.setReviewerId(UserContext.getUserId());
                    sl.setReviewComment("质检合格，自动审核通过");
                    sl.setServiceEndTime(LocalDateTime.now());
                    serviceLogMapper.updateById(sl);
                }
            }
            if (qc.getOrderId() != null) {
                Order order = orderMapper.selectById(qc.getOrderId());
                if (order != null) {
                    order.setStatus("COMPLETED");
                    order.setCompleteTime(LocalDateTime.now());
                    orderMapper.updateById(order);
                }
            }
            qc.setRectifyStatus(null); // 无需整改
        } else {
            // 不合格/需整改：开启整改流程
            qc.setNeedRectify(true);
            qc.setRectifyStatus("PENDING");
            qc.setRectifyNotice(dto.getRectifyNotice());
            if (dto.getRectifyDeadline() != null) {
                qc.setRectifyDeadline(dto.getRectifyDeadline());
            }
        }

        qualityCheckMapper.updateById(qc);
    }

    private QualityCheckVO convertToVO(QualityCheck qualityCheck) {
        if (qualityCheck == null) {
            return null;
        }
        QualityCheckVO vo = new QualityCheckVO();
        vo.setQualityCheckId(qualityCheck.getQualityCheckId());
        vo.setCheckNo(qualityCheck.getCheckNo());
        vo.setOrderId(qualityCheck.getOrderId());
        vo.setOrderNo(qualityCheck.getOrderNo());
        vo.setServiceLogId(qualityCheck.getServiceLogId());
        vo.setServiceCategory(qualityCheck.getServiceCategory());
        vo.setProviderId(qualityCheck.getProviderId());
        vo.setProviderName(qualityCheck.getProviderName());
        vo.setStaffId(qualityCheck.getStaffId());
        vo.setStaffName(qualityCheck.getStaffName());
        vo.setCheckType(qualityCheck.getCheckType());
        vo.setCheckMethod(qualityCheck.getCheckMethod());
        vo.setCheckScore(qualityCheck.getCheckScore());
        vo.setCheckResult(qualityCheck.getCheckResult());
        vo.setCheckPhotos(qualityCheck.getCheckPhotos());
        vo.setCheckRemark(qualityCheck.getCheckRemark());
        vo.setCheckTime(qualityCheck.getCheckTime() != null ? qualityCheck.getCheckTime().toString() : null);
        vo.setCheckerId(qualityCheck.getCheckerId());
        vo.setCheckerName(qualityCheck.getCheckerName());
        vo.setNeedRectify(qualityCheck.getNeedRectify());
        vo.setRectifyNotice(qualityCheck.getRectifyNotice());
        vo.setRectifyDeadline(qualityCheck.getRectifyDeadline() != null ? qualityCheck.getRectifyDeadline().toString() : null);
        vo.setRectifyStatus(qualityCheck.getRectifyStatus());
        vo.setRectifyPhotos(qualityCheck.getRectifyPhotos());
        vo.setRectifyRemark(qualityCheck.getRectifyRemark());
        vo.setRecheckTime(qualityCheck.getRecheckTime() != null ? qualityCheck.getRecheckTime().toString() : null);
        vo.setRecheckResult(qualityCheck.getRecheckResult());
        vo.setCreateTime(qualityCheck.getCreateTime() != null ? qualityCheck.getCreateTime().toString() : null);
        return vo;
    }
}
