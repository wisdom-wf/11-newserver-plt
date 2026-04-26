package com.elderlycare.service.quality.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.quality.QualityImprovement;
import com.elderlycare.mapper.quality.QualityImprovementMapper;
import com.elderlycare.service.quality.QualityImprovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 质量改进服务实现
 */
@Service
@RequiredArgsConstructor
public class QualityImprovementServiceImpl implements QualityImprovementService {

    private final QualityImprovementMapper improvementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createImprovement(QualityImprovement improvement) {
        improvement.setImprovementId(IDGenerator.generateId());
        improvement.setStatus("PENDING");
        improvement.setCreateTime(LocalDateTime.now());
        improvement.setCreatorId(UserContext.getUserId());
        improvement.setCreatorName(UserContext.getUsername());
        improvementMapper.insert(improvement);
        return improvement.getImprovementId();
    }

    @Override
    public PageResult<QualityImprovement> getImprovementList(String providerId, String status, int page, int pageSize) {
        Page<QualityImprovement> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<QualityImprovement> wrapper = new LambdaQueryWrapper<>();

        if (providerId != null && !providerId.isEmpty()) {
            wrapper.eq(QualityImprovement::getProviderId, providerId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(QualityImprovement::getStatus, status);
        }
        wrapper.orderByDesc(QualityImprovement::getCreateTime);

        IPage<QualityImprovement> result = improvementMapper.selectPage(pageObj, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    @Override
    public QualityImprovement getImprovementById(String improvementId) {
        return improvementMapper.selectById(improvementId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImprovement(QualityImprovement improvement) {
        improvement.setUpdateTime(LocalDateTime.now());
        improvementMapper.updateById(improvement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExecution(String improvementId) {
        QualityImprovement improvement = improvementMapper.selectById(improvementId);
        if (improvement == null) {
            throw new RuntimeException("改进计划不存在");
        }
        if (!"PENDING".equals(improvement.getStatus())) {
            throw new RuntimeException("当前状态不允许开始执行");
        }
        improvement.setStatus("IN_PROGRESS");
        improvement.setStartDate(LocalDate.now());
        improvement.setUpdateTime(LocalDateTime.now());
        improvementMapper.updateById(improvement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(String improvementId) {
        QualityImprovement improvement = improvementMapper.selectById(improvementId);
        if (improvement == null) {
            throw new RuntimeException("改进计划不存在");
        }
        if (!"IN_PROGRESS".equals(improvement.getStatus())) {
            throw new RuntimeException("当前状态不允许标记完成");
        }
        improvement.setStatus("COMPLETED");
        improvement.setCompletionDate(LocalDate.now());
        improvement.setUpdateTime(LocalDateTime.now());
        improvementMapper.updateById(improvement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(String improvementId, String effectEvaluation, String evaluationResult) {
        QualityImprovement improvement = improvementMapper.selectById(improvementId);
        if (improvement == null) {
            throw new RuntimeException("改进计划不存在");
        }
        if (!"COMPLETED".equals(improvement.getStatus())) {
            throw new RuntimeException("只能评估已完成的改进计划");
        }
        improvement.setStatus("EVALUATED");
        improvement.setEffectEvaluation(effectEvaluation);
        improvement.setEvaluatorId(UserContext.getUserId());
        improvement.setEvaluatorName(UserContext.getUsername());
        improvement.setEvaluateTime(LocalDateTime.now());
        improvement.setUpdateTime(LocalDateTime.now());
        improvementMapper.updateById(improvement);
    }
}
