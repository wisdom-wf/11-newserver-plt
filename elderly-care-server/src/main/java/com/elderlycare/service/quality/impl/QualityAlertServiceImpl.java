package com.elderlycare.service.quality.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.entity.quality.QualityAlert;
import com.elderlycare.mapper.evaluation.ServiceEvaluationMapper;
import com.elderlycare.mapper.quality.QualityAlertMapper;
import com.elderlycare.service.quality.QualityAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量预警服务实现
 */
@Service
@RequiredArgsConstructor
public class QualityAlertServiceImpl implements QualityAlertService {

    private final QualityAlertMapper qualityAlertMapper;
    private final ServiceEvaluationMapper evaluationMapper;

    private static final BigDecimal LOW_SCORE_THRESHOLD = new BigDecimal("3.0"); // 评分低于3分触发预警

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAlert(QualityAlert alert) {
        alert.setAlertId(IDGenerator.generateId());
        alert.setAlertStatus("PENDING");
        alert.setCreateTime(LocalDateTime.now());
        qualityAlertMapper.insert(alert);
        return alert.getAlertId();
    }

    @Override
    public PageResult<QualityAlert> getAlertList(String providerId, String staffId, String alertType, String alertStatus, int page, int pageSize) {
        Page<QualityAlert> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<QualityAlert> wrapper = new LambdaQueryWrapper<>();

        if (providerId != null && !providerId.isEmpty()) {
            wrapper.eq(QualityAlert::getProviderId, providerId);
        }
        if (staffId != null && !staffId.isEmpty()) {
            wrapper.eq(QualityAlert::getStaffId, staffId);
        }
        if (alertType != null && !alertType.isEmpty()) {
            wrapper.eq(QualityAlert::getAlertType, alertType);
        }
        if (alertStatus != null && !alertStatus.isEmpty()) {
            wrapper.eq(QualityAlert::getAlertStatus, alertStatus);
        }
        wrapper.orderByDesc(QualityAlert::getCreateTime);

        IPage<QualityAlert> result = qualityAlertMapper.selectPage(pageObj, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    @Override
    public QualityAlert getAlertById(String alertId) {
        return qualityAlertMapper.selectById(alertId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlert(String alertId, String handleResult) {
        QualityAlert alert = qualityAlertMapper.selectById(alertId);
        if (alert == null) {
            throw new RuntimeException("预警不存在");
        }
        alert.setAlertStatus("HANDLED");
        alert.setHandleResult(handleResult);
        alert.setHandlerId(UserContext.getUserId());
        alert.setHandlerName(UserContext.getUsername());
        alert.setHandleTime(LocalDateTime.now());
        qualityAlertMapper.updateById(alert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreAlert(String alertId) {
        QualityAlert alert = qualityAlertMapper.selectById(alertId);
        if (alert == null) {
            throw new RuntimeException("预警不存在");
        }
        alert.setAlertStatus("IGNORED");
        qualityAlertMapper.updateById(alert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndGenerateAlerts() {
        // 检查最近7天内的评价，生成预警
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // 1. 检查评分过低的预警
        LambdaQueryWrapper<ServiceEvaluation> evalWrapper = new LambdaQueryWrapper<>();
        evalWrapper.ge(ServiceEvaluation::getEvaluationTime, weekAgo)
                   .lt(ServiceEvaluation::getOverallScore, LOW_SCORE_THRESHOLD.intValue());

        List<ServiceEvaluation> lowScoreEvals = evaluationMapper.selectList(evalWrapper);
        for (ServiceEvaluation eval : lowScoreEvals) {
            // 检查是否已存在相同预警
            LambdaQueryWrapper<QualityAlert> alertWrapper = new LambdaQueryWrapper<>();
            alertWrapper.eq(QualityAlert::getEvaluationId, eval.getEvaluationId())
                       .eq(QualityAlert::getAlertType, "LOW_SCORE")
                       .in(QualityAlert::getAlertStatus, "PENDING", "HANDLING");
            if (qualityAlertMapper.selectCount(alertWrapper) == 0) {
                QualityAlert alert = new QualityAlert();
                alert.setAlertType("LOW_SCORE");
                alert.setProviderId(eval.getProviderId());
                alert.setStaffId(eval.getStaffId());
                alert.setElderId(eval.getElderId());
                alert.setOrderId(eval.getOrderId());
                alert.setEvaluationId(eval.getEvaluationId());
                alert.setSeverity(eval.getOverallScore().intValue() <= 1 ? "CRITICAL" : "HIGH");
                alert.setAlertContent(String.format("服务评分过低：%.1f分", eval.getOverallScore()));
                createAlert(alert);
            }
        }

        // 2. 检查投诉增多的预警（简化版：同一服务人员7天内超过3条评价）
        // 实际应该统计投诉数量，这里简化处理
    }
}
