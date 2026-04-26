package com.elderlycare.service.quality;

import com.elderlycare.common.PageResult;
import com.elderlycare.entity.quality.QualityImprovement;

/**
 * 质量改进服务接口
 */
public interface QualityImprovementService {

    /**
     * 创建改进计划
     */
    String createImprovement(QualityImprovement improvement);

    /**
     * 获取改进列表
     */
    PageResult<QualityImprovement> getImprovementList(String providerId, String status, int page, int pageSize);

    /**
     * 获取改进详情
     */
    QualityImprovement getImprovementById(String improvementId);

    /**
     * 更新改进计划
     */
    void updateImprovement(QualityImprovement improvement);

    /**
     * 开始执行
     */
    void startExecution(String improvementId);

    /**
     * 标记完成
     */
    void complete(String improvementId);

    /**
     * 评估效果
     */
    void evaluate(String improvementId, String effectEvaluation, String evaluationResult);
}
