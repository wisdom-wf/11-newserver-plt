package com.elderlycare.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.evaluation.ServiceEvaluationMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.evaluation.ServiceEvaluationService;
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
        evaluation.setElderName(order.getElderName());
        evaluation.setProviderId(order.getProviderId());
        evaluation.setServiceTypeCode(order.getServiceTypeCode());
        evaluation.setServiceTypeName(order.getServiceTypeName());
        evaluation.setStatus(1); // 默认显示

        // 设置默认评分
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
        ServiceEvaluation evaluation = evaluationMapper.selectById(evaluationId);
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
            totalRating += e.getRating();
            totalAttitude += e.getAttitudeScore() != null ? e.getAttitudeScore() : e.getRating();
            totalQuality += e.getQualityScore() != null ? e.getQualityScore() : e.getRating();
            totalEfficiency += e.getEfficiencyScore() != null ? e.getEfficiencyScore() : e.getRating();

            switch (e.getRating()) {
                case 5: fiveStar++; break;
                case 4: fourStar++; break;
                case 3: threeStar++; break;
                case 2: twoStar++; break;
                case 1: oneStar++; break;
            }
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
            totalRating += e.getRating();
            totalAttitude += e.getAttitudeScore() != null ? e.getAttitudeScore() : e.getRating();
            totalQuality += e.getQualityScore() != null ? e.getQualityScore() : e.getRating();
            totalEfficiency += e.getEfficiencyScore() != null ? e.getEfficiencyScore() : e.getRating();

            switch (e.getRating()) {
                case 5: fiveStar++; break;
                case 4: fourStar++; break;
                case 3: threeStar++; break;
                case 2: twoStar++; break;
                case 1: oneStar++; break;
            }
        }

        // Get provider info from first evaluation
        ServiceEvaluation first = evaluations.get(0);
        vo.setProviderId(first.getProviderId());
        vo.setProviderName(first.getProviderName());
        vo.setStaffName(first.getStaffName());

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
        if (StringUtils.isNotBlank(dto.getServiceTypeCode())) {
            wrapper.eq(ServiceEvaluation::getServiceTypeCode, dto.getServiceTypeCode());
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
        Map<String, List<ServiceEvaluation>> byServiceType = new HashMap<>();
        Map<String, List<ServiceEvaluation>> byProvider = new HashMap<>();

        for (ServiceEvaluation e : evaluations) {
            totalRating += e.getRating();

            switch (e.getRating()) {
                case 5: fiveStar++; break;
                case 4: fourStar++; break;
                case 3: threeStar++; break;
                case 2: twoStar++; break;
                case 1: oneStar++; break;
            }

            // Group by service type
            String serviceType = e.getServiceTypeCode();
            byServiceType.computeIfAbsent(serviceType, k -> new ArrayList<>()).add(e);

            // Group by provider
            String provider = e.getProviderId();
            byProvider.computeIfAbsent(provider, k -> new ArrayList<>()).add(e);
        }

        int count = evaluations.size();
        vo.setTotalCount((long) count);
        vo.setAverageRating(BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        vo.setFiveStarCount(fiveStar);
        vo.setFourStarCount(fourStar);
        vo.setThreeStarCount(threeStar);
        vo.setTwoStarCount(twoStar);
        vo.setOneStarCount(oneStar);

        // Calculate service type statistics
        List<EvaluationStatisticsVO.ServiceTypeStatistics> serviceTypeStats = new ArrayList<>();
        for (Map.Entry<String, List<ServiceEvaluation>> entry : byServiceType.entrySet()) {
            EvaluationStatisticsVO.ServiceTypeStatistics stat = new EvaluationStatisticsVO.ServiceTypeStatistics();
            stat.setServiceTypeCode(entry.getKey());
            stat.setCount((long) entry.getValue().size());

            int typeTotal = entry.getValue().stream().mapToInt(ServiceEvaluation::getRating).sum();
            stat.setAverageRating(BigDecimal.valueOf(typeTotal).divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP));

            // Get service type name from first entry
            stat.setServiceTypeName(entry.getValue().get(0).getServiceTypeName());
            serviceTypeStats.add(stat);
        }
        vo.setByServiceType(serviceTypeStats);

        // Calculate provider statistics
        List<EvaluationStatisticsVO.ProviderStatistics> providerStats = new ArrayList<>();
        for (Map.Entry<String, List<ServiceEvaluation>> entry : byProvider.entrySet()) {
            EvaluationStatisticsVO.ProviderStatistics stat = new EvaluationStatisticsVO.ProviderStatistics();
            stat.setProviderId(entry.getKey());
            stat.setCount((long) entry.getValue().size());

            int typeTotal = entry.getValue().stream().mapToInt(ServiceEvaluation::getRating).sum();
            stat.setAverageRating(BigDecimal.valueOf(typeTotal).divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP));

            // Get provider name from first entry
            stat.setProviderName(entry.getValue().get(0).getProviderName());
            providerStats.add(stat);
        }
        vo.setByProvider(providerStats);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyEvaluation(String evaluationId, String replyContent) {
        ServiceEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw BusinessException.notFound("评价不存在");
        }

        evaluation.setReplyContent(replyContent);
        evaluation.setReplyTime(LocalDateTime.now());
        evaluationMapper.updateById(evaluation);
    }
}
