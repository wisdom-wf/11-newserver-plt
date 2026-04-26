package com.elderlycare.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.evaluation.ServiceEvaluationMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.evaluation.ServiceEvaluationService;
import com.elderlycare.vo.evaluation.EvaluationInviteVO;
import com.elderlycare.vo.evaluation.EvaluationStatisticsVO;
import com.elderlycare.vo.evaluation.EvaluationVO;
import com.elderlycare.vo.evaluation.ProviderScoreVO;
import com.elderlycare.vo.evaluation.StaffScoreVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务评价Service实现
 */
@Service
@RequiredArgsConstructor
public class ServiceEvaluationServiceImpl extends ServiceImpl<ServiceEvaluationMapper, ServiceEvaluation>
        implements ServiceEvaluationService {

    private final ServiceEvaluationMapper evaluationMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createEvaluation(CreateEvaluationDTO dto) {
        // 查询订单信息
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }

        // 检查是否已评价
        LambdaQueryWrapper<ServiceEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceEvaluation::getOrderId, dto.getOrderId());
        if (evaluationMapper.selectCount(wrapper) > 0) {
            throw BusinessException.fail("该订单已评价，不能重复评价");
        }

        ServiceEvaluation evaluation = new ServiceEvaluation();
        BeanUtils.copyProperties(dto, evaluation);

        // 设置订单相关信息
        evaluation.setElderId(order.getElderId());
        evaluation.setProviderId(order.getProviderId());
        evaluation.setStaffId(order.getStaffId());

        // 设置总体评分
        evaluation.setOverallScore(dto.getRating());

        // 设置各维度评分
        if (evaluation.getAttitudeScore() == null) {
            evaluation.setAttitudeScore(dto.getRating());
        }
        if (evaluation.getQualityScore() == null) {
            evaluation.setQualityScore(dto.getRating());
        }
        if (evaluation.getEfficiencyScore() == null) {
            evaluation.setEfficiencyScore(dto.getRating());
        }

        evaluationMapper.insert(evaluation);
        return evaluation.getEvaluationId();
    }

    @Override
    public PageResult<ServiceEvaluation> queryEvaluations(EvaluationQueryDTO dto) {
        Page<ServiceEvaluation> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<ServiceEvaluation> result = evaluationMapper.selectEvaluationPage(page, dto);

        return PageResult.of(result.getTotal(), dto.getPage(), dto.getPageSize(), result.getRecords());
    }

    @Override
    public EvaluationVO getEvaluationById(String evaluationId) {
        ServiceEvaluation evaluation = evaluationMapper.selectEvaluationDetail(evaluationId);
        if (evaluation == null) {
            throw BusinessException.notFound("评价不存在");
        }

        EvaluationVO vo = new EvaluationVO();
        BeanUtils.copyProperties(evaluation, vo);
        return vo;
    }

    @Override
    public ProviderScoreVO getProviderScore(String providerId) {
        LambdaQueryWrapper<ServiceEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceEvaluation::getProviderId, providerId);
        List<ServiceEvaluation> evaluations = evaluationMapper.selectList(wrapper);

        ProviderScoreVO vo = new ProviderScoreVO();
        vo.setProviderId(providerId);

        if (evaluations == null || evaluations.isEmpty()) {
            vo.setAverageRating(BigDecimal.ZERO);
            vo.setEvaluationCount(0);
            vo.setFiveStarCount(0);
            vo.setFourStarCount(0);
            vo.setThreeStarCount(0);
            vo.setTwoStarCount(0);
            vo.setOneStarCount(0);
            vo.setAverageAttitudeScore(BigDecimal.ZERO);
            vo.setAverageQualityScore(BigDecimal.ZERO);
            vo.setAverageEfficiencyScore(BigDecimal.ZERO);
            return vo;
        }

        int totalRating = 0;
        int totalAttitude = 0;
        int totalQuality = 0;
        int totalEfficiency = 0;
        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;

        for (ServiceEvaluation e : evaluations) {
            Integer rating = e.getOverallScore();
            if (rating != null) {
                totalRating += rating;
                switch (rating) {
                    case 5: fiveStar++; break;
                    case 4: fourStar++; break;
                    case 3: threeStar++; break;
                    case 2: twoStar++; break;
                    case 1: oneStar++; break;
                }
            }
            totalAttitude += e.getAttitudeScore() != null ? e.getAttitudeScore() : 0;
            totalQuality += e.getQualityScore() != null ? e.getQualityScore() : 0;
            totalEfficiency += e.getEfficiencyScore() != null ? e.getEfficiencyScore() : 0;
        }

        int count = evaluations.size();
        vo.setEvaluationCount(count);
        vo.setAverageRating(BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setAverageAttitudeScore(BigDecimal.valueOf(totalAttitude).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setAverageQualityScore(BigDecimal.valueOf(totalQuality).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setAverageEfficiencyScore(BigDecimal.valueOf(totalEfficiency).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setFiveStarCount(fiveStar);
        vo.setFourStarCount(fourStar);
        vo.setThreeStarCount(threeStar);
        vo.setTwoStarCount(twoStar);
        vo.setOneStarCount(oneStar);

        return vo;
    }

    @Override
    public StaffScoreVO getStaffScore(String staffId) {
        LambdaQueryWrapper<ServiceEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceEvaluation::getStaffId, staffId);
        List<ServiceEvaluation> evaluations = evaluationMapper.selectList(wrapper);

        StaffScoreVO vo = new StaffScoreVO();
        vo.setStaffId(staffId);

        if (evaluations == null || evaluations.isEmpty()) {
            vo.setAverageRating(BigDecimal.ZERO);
            vo.setEvaluationCount(0);
            vo.setFiveStarCount(0);
            vo.setFourStarCount(0);
            vo.setThreeStarCount(0);
            vo.setTwoStarCount(0);
            vo.setOneStarCount(0);
            vo.setAverageAttitudeScore(BigDecimal.ZERO);
            vo.setAverageQualityScore(BigDecimal.ZERO);
            vo.setAverageEfficiencyScore(BigDecimal.ZERO);
            return vo;
        }

        int totalRating = 0;
        int totalAttitude = 0;
        int totalQuality = 0;
        int totalEfficiency = 0;
        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;

        for (ServiceEvaluation e : evaluations) {
            Integer rating = e.getOverallScore();
            if (rating != null) {
                totalRating += rating;
                switch (rating) {
                    case 5: fiveStar++; break;
                    case 4: fourStar++; break;
                    case 3: threeStar++; break;
                    case 2: twoStar++; break;
                    case 1: oneStar++; break;
                }
            }
            totalAttitude += e.getAttitudeScore() != null ? e.getAttitudeScore() : 0;
            totalQuality += e.getQualityScore() != null ? e.getQualityScore() : 0;
            totalEfficiency += e.getEfficiencyScore() != null ? e.getEfficiencyScore() : 0;
        }

        // Get provider info from first evaluation
        ServiceEvaluation first = evaluations.get(0);
        vo.setProviderId(first.getProviderId());

        int count = evaluations.size();
        vo.setEvaluationCount(count);
        vo.setAverageRating(BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setAverageAttitudeScore(BigDecimal.valueOf(totalAttitude).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setAverageQualityScore(BigDecimal.valueOf(totalQuality).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setAverageEfficiencyScore(BigDecimal.valueOf(totalEfficiency).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setFiveStarCount(fiveStar);
        vo.setFourStarCount(fourStar);
        vo.setThreeStarCount(threeStar);
        vo.setTwoStarCount(twoStar);
        vo.setOneStarCount(oneStar);

        return vo;
    }

    @Override
    public EvaluationStatisticsVO getStatistics(EvaluationStatisticsDTO dto) {
        LambdaQueryWrapper<ServiceEvaluation> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(dto.getProviderId())) {
            wrapper.eq(ServiceEvaluation::getProviderId, dto.getProviderId());
        }

        List<ServiceEvaluation> evaluations = evaluationMapper.selectList(wrapper);

        EvaluationStatisticsVO vo = new EvaluationStatisticsVO();

        if (evaluations == null || evaluations.isEmpty()) {
            vo.setTotalCount(0L);
            vo.setAverageRating(BigDecimal.ZERO);
            vo.setFiveStarCount(0L);
            vo.setFourStarCount(0L);
            vo.setThreeStarCount(0L);
            vo.setTwoStarCount(0L);
            vo.setOneStarCount(0L);
            vo.setByServiceType(new ArrayList<>());
            vo.setByProvider(new ArrayList<>());
            return vo;
        }

        // Calculate overall statistics
        int totalRating = 0;
        long fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;

        for (ServiceEvaluation e : evaluations) {
            Integer rating = e.getOverallScore();
            if (rating != null) {
                totalRating += rating;
                switch (rating) {
                    case 5: fiveStar++; break;
                    case 4: fourStar++; break;
                    case 3: threeStar++; break;
                    case 2: twoStar++; break;
                    case 1: oneStar++; break;
                }
            }
        }

        int count = evaluations.size();
        vo.setTotalCount((long) count);
        if (count > 0) {
            vo.setAverageRating(BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAverageRating(BigDecimal.ZERO);
        }
        vo.setFiveStarCount(fiveStar);
        vo.setFourStarCount(fourStar);
        vo.setThreeStarCount(threeStar);
        vo.setTwoStarCount(twoStar);
        vo.setOneStarCount(oneStar);
        vo.setByServiceType(new ArrayList<>());
        vo.setByProvider(new ArrayList<>());

        return vo;
    }

    @Override
    public ServiceEvaluation getEvaluationByOrderId(String orderId) {
        LambdaQueryWrapper<ServiceEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceEvaluation::getOrderId, orderId)
                .orderByDesc(ServiceEvaluation::getCreateTime)
                .last("LIMIT 1");
        return evaluationMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyEvaluation(String evaluationId, String replyContent) {
        ServiceEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw BusinessException.notFound("评价不存在");
        }

        // 设置回复内容
        evaluation.setReplyContent(replyContent);
        evaluation.setReplyTime(LocalDateTime.now());
        // 从UserContext获取回复人信息
        evaluation.setReplyerId(com.elderlycare.common.UserContext.getUserId());
        evaluation.setReplyerName(com.elderlycare.common.UserContext.getUsername());
        evaluationMapper.updateById(evaluation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationInviteVO generateEvaluationLink(String orderId, String elderId, String elderName, Integer expireHours) {
        // 查询订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }

        // 生成Token
        String token = generateSecureToken();

        // 创建评价记录（待评价状态）
        ServiceEvaluation evaluation = new ServiceEvaluation();
        evaluation.setEvaluationId(IDGenerator.generateId());
        evaluation.setOrderId(orderId);
        evaluation.setElderId(elderId);
        evaluation.setElderName(elderName);
        evaluation.setProviderId(order.getProviderId());
        evaluation.setProviderName(order.getProviderName());
        evaluation.setStaffId(order.getStaffId());
        evaluation.setStaffName(order.getStaffName());
        evaluation.setServiceTypeCode(order.getServiceTypeCode());
        evaluation.setServiceTypeName(order.getServiceTypeName());
        evaluation.setEvaluationToken(token);
        evaluation.setTokenStatus("PENDING");
        evaluation.setTokenExpireTime(LocalDateTime.now().plusHours(expireHours != null ? expireHours : 72));
        evaluation.setCreateTime(LocalDateTime.now());
        evaluationMapper.insert(evaluation);

        // 构建返回VO
        EvaluationInviteVO vo = new EvaluationInviteVO();
        vo.setEvaluationId(evaluation.getEvaluationId());
        vo.setToken(token);
        vo.setSurveyUrl("/public/survey?token=" + token);
        vo.setOrderId(orderId);
        vo.setElderId(elderId);
        vo.setElderName(elderName);
        vo.setProviderId(order.getProviderId());
        vo.setProviderName(order.getProviderName());
        vo.setStaffId(order.getStaffId());
        vo.setStaffName(order.getStaffName());
        vo.setServiceType(order.getServiceTypeName());
        vo.setTokenStatus("PENDING");
        vo.setTokenExpireTime(evaluation.getTokenExpireTime());
        vo.setCreateTime(evaluation.getCreateTime());
        return vo;
    }

    @Override
    public EvaluationInviteVO validateToken(String token) {
        ServiceEvaluation evaluation = evaluationMapper.selectByToken(token);
        if (evaluation == null) {
            throw BusinessException.notFound("评价链接不存在");
        }

        // 检查Token状态
        String status = evaluation.getTokenStatus();
        if ("COMPLETED".equals(status)) {
            throw BusinessException.fail("该评价链接已被使用");
        }
        if ("EXPIRED".equals(status)) {
            throw BusinessException.fail("该评价链接已过期");
        }
        if (!"PENDING".equals(status)) {
            throw BusinessException.fail("该评价链接已失效");
        }

        // 检查是否过期
        if (evaluation.getTokenExpireTime() != null && evaluation.getTokenExpireTime().isBefore(LocalDateTime.now())) {
            // 更新状态为过期
            evaluation.setTokenStatus("EXPIRED");
            evaluationMapper.updateById(evaluation);
            throw BusinessException.fail("该评价链接已过期");
        }

        EvaluationInviteVO vo = new EvaluationInviteVO();
        vo.setEvaluationId(evaluation.getEvaluationId());
        vo.setToken(token);
        vo.setSurveyUrl("/public/survey?token=" + token);
        vo.setOrderId(evaluation.getOrderId());
        vo.setElderId(evaluation.getElderId());
        vo.setElderName(evaluation.getElderName());
        vo.setProviderId(evaluation.getProviderId());
        vo.setProviderName(evaluation.getProviderName());
        vo.setStaffId(evaluation.getStaffId());
        vo.setStaffName(evaluation.getStaffName());
        vo.setTokenStatus(evaluation.getTokenStatus());
        vo.setTokenExpireTime(evaluation.getTokenExpireTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitSurveyByToken(String token, SubmitSurveyDTO form, String ipAddress) {
        ServiceEvaluation evaluation = evaluationMapper.selectByToken(token);
        if (evaluation == null) {
            throw BusinessException.notFound("评价链接不存在");
        }

        // 检查Token状态
        String status = evaluation.getTokenStatus();
        if (!"PENDING".equals(status)) {
            throw BusinessException.fail("该评价链接已失效，无法提交");
        }

        // 检查是否过期
        if (evaluation.getTokenExpireTime() != null && evaluation.getTokenExpireTime().isBefore(LocalDateTime.now())) {
            evaluation.setTokenStatus("EXPIRED");
            evaluationMapper.updateById(evaluation);
            throw BusinessException.fail("该评价链接已过期");
        }

        // 设置评分
        evaluation.setOverallScore(form.getServiceScore());
        evaluation.setAttitudeScore(form.getAttitudeScore());
        evaluation.setQualityScore(form.getSkillScore());
        evaluation.setEfficiencyScore(form.getPunctualityScore());
        evaluation.setEnvironmentScore(form.getEnvironmentScore());

        // 计算平均评分
        BigDecimal avgScore = BigDecimal.valueOf(
            (form.getServiceScore() + form.getAttitudeScore() + form.getSkillScore() + form.getPunctualityScore()) / 4.0
        );
        evaluation.setAverageScore(avgScore);

        // 设置评价内容
        evaluation.setContent(form.getContent());
        if (form.getTags() != null && !form.getTags().isEmpty()) {
            evaluation.setTags(String.join(",", form.getTags()));
        }
        if (form.getImages() != null && !form.getImages().isEmpty()) {
            evaluation.setTags(String.join(",", form.getImages()));
        }
        evaluation.setAnonymous(form.getAnonymous() != null && form.getAnonymous() ? 1 : 0);
        evaluation.setEvaluationTime(LocalDateTime.now());

        // 更新Token状态
        evaluation.setTokenStatus("COMPLETED");
        evaluation.setTokenUsedTime(LocalDateTime.now());
        evaluation.setTokenUsedIp(ipAddress);

        evaluationMapper.updateById(evaluation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidateInvite(String token) {
        ServiceEvaluation evaluation = evaluationMapper.selectByToken(token);
        if (evaluation == null) {
            throw BusinessException.notFound("评价链接不存在");
        }

        if (!"PENDING".equals(evaluation.getTokenStatus())) {
            throw BusinessException.fail("该链接已无法作废");
        }

        evaluation.setTokenStatus("INVALID");
        evaluationMapper.updateById(evaluation);
    }

    /**
     * 生成安全的随机Token
     */
    private String generateSecureToken() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02X", b));
        }
        return token.toString();
    }
}
