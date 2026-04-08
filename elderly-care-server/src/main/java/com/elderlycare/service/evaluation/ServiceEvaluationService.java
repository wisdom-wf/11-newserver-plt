package com.elderlycare.service.evaluation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.vo.evaluation.EvaluationStatisticsVO;
import com.elderlycare.vo.evaluation.EvaluationVO;
import com.elderlycare.vo.evaluation.ProviderScoreVO;
import com.elderlycare.vo.evaluation.StaffScoreVO;

/**
 * 服务评价Service接口
 */
public interface ServiceEvaluationService extends IService<ServiceEvaluation> {

    /**
     * 创建评价
     */
    String createEvaluation(CreateEvaluationDTO dto);

    /**
     * 分页查询评价
     */
    PageResult<ServiceEvaluation> queryEvaluations(EvaluationQueryDTO dto);

    /**
     * 获取评价详情
     */
    EvaluationVO getEvaluationById(String evaluationId);

    /**
     * 获取服务商评分
     */
    ProviderScoreVO getProviderScore(String providerId);

    /**
     * 获取服务人员评分
     */
    StaffScoreVO getStaffScore(String staffId);

    /**
     * 评价统计
     */
    EvaluationStatisticsVO getStatistics(EvaluationStatisticsDTO dto);

    /**
     * 回复评价
     */
    void replyEvaluation(String evaluationId, String replyContent);
}
