package com.elderlycare.service.quality.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.quality.QualityCheckQueryDTO;
import com.elderlycare.entity.quality.QualityCheck;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.service.quality.QualityCheckService;
import com.elderlycare.vo.quality.QualityCheckStatisticsVO;
import com.elderlycare.vo.quality.QualityCheckVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        qualityCheck.setCheckResult(vo.getCheckResult());
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
            qualityCheck.setRectifyStatus("IN_PROGRESS");
            qualityCheckMapper.updateById(qualityCheck);
        }
    }

    @Override
    public void recheck(String id, Map<String, Object> params) {
        QualityCheck qualityCheck = qualityCheckMapper.selectById(id);
        if (qualityCheck != null) {
            qualityCheck.setRecheckResult((String) params.get("result"));
            qualityCheck.setRectifyRemark((String) params.get("remark"));
            qualityCheck.setRecheckTime(LocalDateTime.now());
            if ("PASSED".equals(params.get("result"))) {
                qualityCheck.setRectifyStatus("VERIFIED");
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

        // 平均评分
        BigDecimal avgScore = qualityCheckMapper.avgCheckScore();
        stats.setAvgScore(avgScore != null ? avgScore : BigDecimal.ZERO);

        return stats;
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
