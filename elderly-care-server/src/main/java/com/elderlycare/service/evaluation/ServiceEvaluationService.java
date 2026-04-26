package com.elderlycare.service.evaluation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.vo.evaluation.EvaluationInviteVO;
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
     * 根据订单ID查询最新评价
     */
    ServiceEvaluation getEvaluationByOrderId(String orderId);

    /**
     * 回复评价
     */
    void replyEvaluation(String evaluationId, String replyContent);

    /**
     * 生成评价邀请链接
     * @param orderId 订单ID
     * @param elderId 老人ID
     * @param elderName 老人姓名
     * @param expireHours 过期小时数，默认72
     * @return 包含token和链接的信息
     */
    EvaluationInviteVO generateEvaluationLink(String orderId, String elderId, String elderName, Integer expireHours);

    /**
     * 验证Token获取问卷信息
     * @param token 评价邀请Token
     * @return 问卷信息
     */
    EvaluationInviteVO validateToken(String token);

    /**
     * 提交问卷评价
     * @param token 评价邀请Token
     * @param form 评价表单
     * @param ipAddress 提交IP地址
     */
    void submitSurveyByToken(String token, SubmitSurveyDTO form, String ipAddress);

    /**
     * 作废评价邀请链接
     * @param token 评价邀请Token
     */
    void invalidateInvite(String token);
}
