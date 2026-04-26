package com.elderlycare.service.staff.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.PageResult;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.entity.staff.Staff;
import com.elderlycare.entity.staff.StaffScore;
import com.elderlycare.mapper.evaluation.ServiceEvaluationMapper;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.mapper.staff.StaffScoreMapper;
import com.elderlycare.service.staff.StaffScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 服务人员评分服务实现
 */
@Service
@RequiredArgsConstructor
public class StaffScoreServiceImpl implements StaffScoreService {

    private final StaffScoreMapper staffScoreMapper;
    private final ServiceEvaluationMapper evaluationMapper;
    private final StaffMapper staffMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateScore(String staffId, String periodType) {
        LocalDate today = LocalDate.now();
        LocalDate periodStart;
        LocalDate periodEnd;

        // 计算周期范围
        switch (periodType) {
            case "DAY":
                periodStart = today;
                periodEnd = today;
                break;
            case "WEEK":
                periodStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                periodEnd = periodStart.plusDays(6);
                break;
            case "MONTH":
                periodStart = today.withDayOfMonth(1);
                periodEnd = today.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "QUARTER":
                int currentQuarter = (today.getMonthValue() - 1) / 3;
                periodStart = LocalDate.of(today.getYear(), currentQuarter * 3 + 1, 1);
                periodEnd = periodStart.plusMonths(3).minusDays(1);
                break;
            case "YEAR":
                periodStart = today.withDayOfYear(1);
                periodEnd = today.with(TemporalAdjusters.lastDayOfYear());
                break;
            default:
                throw new RuntimeException("不支持的评分周期类型");
        }

        // 查询该周期内的评价
        LambdaQueryWrapper<ServiceEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceEvaluation::getStaffId, staffId)
               .ge(ServiceEvaluation::getEvaluationTime, periodStart.atStartOfDay())
               .le(ServiceEvaluation::getEvaluationTime, periodEnd.atTime(23, 59, 59));

        List<ServiceEvaluation> evaluations = evaluationMapper.selectList(wrapper);

        // 获取服务人员信息
        Staff staff = staffMapper.selectById(staffId);

        StaffScore score = new StaffScore();
        score.setStaffId(staffId);
        if (staff != null) {
            score.setStaffName(staff.getStaffName());
            score.setProviderId(staff.getProviderId());
        }
        score.setPeriodType(periodType);
        score.setPeriodStart(periodStart);
        score.setPeriodEnd(periodEnd);
        score.setEvaluationCount(evaluations.size());

        if (!evaluations.isEmpty()) {
            BigDecimal attitudeSum = BigDecimal.ZERO;
            BigDecimal qualitySum = BigDecimal.ZERO;
            BigDecimal efficiencySum = BigDecimal.ZERO;
            BigDecimal environmentSum = BigDecimal.ZERO;
            BigDecimal overallSum = BigDecimal.ZERO;

            for (ServiceEvaluation eval : evaluations) {
                if (eval.getAttitudeScore() != null) {
                    attitudeSum = attitudeSum.add(BigDecimal.valueOf(eval.getAttitudeScore()));
                }
                if (eval.getQualityScore() != null) {
                    qualitySum = qualitySum.add(BigDecimal.valueOf(eval.getQualityScore()));
                }
                if (eval.getEfficiencyScore() != null) {
                    efficiencySum = efficiencySum.add(BigDecimal.valueOf(eval.getEfficiencyScore()));
                }
                if (eval.getEnvironmentScore() != null) {
                    environmentSum = environmentSum.add(BigDecimal.valueOf(eval.getEnvironmentScore()));
                }
                if (eval.getOverallScore() != null) {
                    overallSum = overallSum.add(BigDecimal.valueOf(eval.getOverallScore()));
                }
            }

            int count = evaluations.size();
            score.setAttitudeScore(attitudeSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            score.setQualityScore(qualitySum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            score.setEfficiencyScore(efficiencySum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            score.setEnvironmentScore(environmentSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            score.setOverallScore(overallSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        } else {
            score.setAttitudeScore(BigDecimal.ZERO);
            score.setQualityScore(BigDecimal.ZERO);
            score.setEfficiencyScore(BigDecimal.ZERO);
            score.setEnvironmentScore(BigDecimal.ZERO);
            score.setOverallScore(BigDecimal.ZERO);
        }

        score.setComplaintCount(0);
        score.setCreateTime(LocalDateTime.now());

        staffScoreMapper.insert(score);
    }

    @Override
    public PageResult<StaffScore> getScoreList(String staffId, String providerId, String periodType, int page, int pageSize) {
        Page<StaffScore> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<StaffScore> wrapper = new LambdaQueryWrapper<>();

        if (staffId != null && !staffId.isEmpty()) {
            wrapper.eq(StaffScore::getStaffId, staffId);
        }
        if (providerId != null && !providerId.isEmpty()) {
            wrapper.eq(StaffScore::getProviderId, providerId);
        }
        if (periodType != null && !periodType.isEmpty()) {
            wrapper.eq(StaffScore::getPeriodType, periodType);
        }
        wrapper.orderByDesc(StaffScore::getCreateTime);

        IPage<StaffScore> result = staffScoreMapper.selectPage(pageObj, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    @Override
    public StaffScore getLatestScore(String staffId) {
        LambdaQueryWrapper<StaffScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffScore::getStaffId, staffId)
               .orderByDesc(StaffScore::getCreateTime)
               .last("LIMIT 1");
        return staffScoreMapper.selectOne(wrapper);
    }

    @Override
    public List<StaffScore> getScoreTrend(String staffId, String periodType, int months) {
        LambdaQueryWrapper<StaffScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffScore::getStaffId, staffId);
        if (periodType != null && !periodType.isEmpty()) {
            wrapper.eq(StaffScore::getPeriodType, periodType);
        }
        wrapper.ge(StaffScore::getPeriodStart, LocalDate.now().minusMonths(months))
               .orderByAsc(StaffScore::getPeriodStart);

        return staffScoreMapper.selectList(wrapper);
    }
}
