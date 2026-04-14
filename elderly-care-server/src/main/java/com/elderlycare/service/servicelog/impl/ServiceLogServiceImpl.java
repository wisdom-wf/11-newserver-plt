package com.elderlycare.service.servicelog.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.servicelog.ServiceLogQueryDTO;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.service.servicelog.ServiceLogService;
import com.elderlycare.vo.servicelog.ServiceLogStatisticsVO;
import com.elderlycare.vo.servicelog.ServiceLogVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务日志服务实现
 */
@Service
@RequiredArgsConstructor
public class ServiceLogServiceImpl implements ServiceLogService {

    private final ServiceLogMapper serviceLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<ServiceLogVO> getServiceLogList(ServiceLogQueryDTO query) {
        Page<ServiceLog> page = new Page<>(query.getCurrent(), query.getPageSize());
        LambdaQueryWrapper<ServiceLog> wrapper = new LambdaQueryWrapper<>();

        if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
            wrapper.eq(ServiceLog::getOrderNo, query.getOrderNo());
        }
        if (query.getElderName() != null && !query.getElderName().isEmpty()) {
            wrapper.like(ServiceLog::getElderName, query.getElderName());
        }
        if (query.getStaffName() != null && !query.getStaffName().isEmpty()) {
            wrapper.like(ServiceLog::getStaffName, query.getStaffName());
        }
        if (query.getServiceType() != null && !query.getServiceType().isEmpty()) {
            wrapper.eq(ServiceLog::getServiceTypeCode, query.getServiceType());
        }
        if (query.getServiceStatus() != null && !query.getServiceStatus().isEmpty()) {
            wrapper.eq(ServiceLog::getServiceStatus, query.getServiceStatus());
        }
        if (query.getProviderId() != null && !query.getProviderId().isEmpty()) {
            wrapper.eq(ServiceLog::getProviderId, query.getProviderId());
        }
        if (query.getStaffId() != null && !query.getStaffId().isEmpty()) {
            wrapper.eq(ServiceLog::getStaffId, query.getStaffId());
        }

        wrapper.orderByDesc(ServiceLog::getCreateTime);

        IPage<ServiceLog> result = serviceLogMapper.selectPage(page, wrapper);

        return new PageResult<>(
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList())
        );
    }

    @Override
    public ServiceLogVO getServiceLog(String id) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        return convertToVO(serviceLog);
    }

    @Override
    public ServiceLogVO getServiceLogByOrderId(String orderId) {
        LambdaQueryWrapper<ServiceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceLog::getOrderId, orderId);
        ServiceLog serviceLog = serviceLogMapper.selectOne(wrapper);
        return convertToVO(serviceLog);
    }

    @Override
    public void submitServiceLog(ServiceLogVO vo) {
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setLogNo("SL" + System.currentTimeMillis());
        serviceLog.setOrderId(vo.getOrderId());
        serviceLog.setElderId(vo.getElderId());
        serviceLog.setElderName(vo.getElderName());
        serviceLog.setElderPhone(vo.getElderPhone());
        serviceLog.setStaffId(vo.getStaffId());
        serviceLog.setStaffName(vo.getStaffName());
        serviceLog.setProviderId(vo.getProviderId());
        serviceLog.setServiceTypeCode(vo.getServiceCategory());
        serviceLog.setServiceTypeName(vo.getServiceType());
        serviceLog.setServiceDate(vo.getServiceDate());
        serviceLog.setServiceStartTime(vo.getServiceStartTime() != null ? LocalDateTime.parse(vo.getServiceStartTime()) : null);
        serviceLog.setServiceEndTime(vo.getServiceEndTime() != null ? LocalDateTime.parse(vo.getServiceEndTime()) : null);
        serviceLog.setServiceDuration(vo.getServiceDuration());
        serviceLog.setServiceStatus(vo.getStatus() != null ? vo.getStatus() : "DRAFT");
        serviceLog.setServiceComment(vo.getServiceContent());
        if (vo.getServicePhotos() != null) {
            try {
                serviceLog.setServicePhotos(objectMapper.writeValueAsString(vo.getServicePhotos()));
            } catch (JsonProcessingException e) {
                serviceLog.setServicePhotos(String.join(",", vo.getServicePhotos()));
            }
        }
        serviceLogMapper.insert(serviceLog);
    }

    @Override
    public void updateServiceLog(String id, ServiceLogVO vo) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if ("VERIFIED".equals(serviceLog.getServiceStatus())) {
            throw new RuntimeException("已审核的日志不能更新");
        }
        if (vo.getServiceStartTime() != null) {
            serviceLog.setServiceStartTime(LocalDateTime.parse(vo.getServiceStartTime()));
        }
        if (vo.getServiceEndTime() != null) {
            serviceLog.setServiceEndTime(LocalDateTime.parse(vo.getServiceEndTime()));
        }
        serviceLog.setServiceDuration(vo.getServiceDuration());
        serviceLog.setServiceStatus(vo.getStatus() != null ? vo.getStatus() : serviceLog.getServiceStatus());
        serviceLog.setServiceComment(vo.getServiceContent());
        if (vo.getServicePhotos() != null) {
            try {
                serviceLog.setServicePhotos(objectMapper.writeValueAsString(vo.getServicePhotos()));
            } catch (JsonProcessingException e) {
                serviceLog.setServicePhotos(String.join(",", vo.getServicePhotos()));
            }
        }
        serviceLogMapper.updateById(serviceLog);
    }

    @Override
    public void submitForReview(String id, String reviewRemarks) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if (!"DRAFT".equals(serviceLog.getServiceStatus())) {
            throw new RuntimeException("只有草稿状态才能提交审核");
        }
        serviceLog.setServiceStatus("SUBMITTED");
        serviceLog.setReviewRemarks(reviewRemarks);
        serviceLog.setAuditStatus("PENDING");
        serviceLogMapper.updateById(serviceLog);
    }

    @Override
    public void deleteServiceLog(String id) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if ("VERIFIED".equals(serviceLog.getServiceStatus()) || "SUBMITTED".equals(serviceLog.getServiceStatus())) {
            throw new RuntimeException("已提交或已审核的日志不能删除");
        }
        serviceLogMapper.deleteById(id);
    }

    @Override
    public void reportAnomaly(String id, Map<String, Object> params) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog != null) {
            serviceLog.setAnomalyType((String) params.get("anomalyType"));
            serviceLog.setAnomalyDesc((String) params.get("anomalyDesc"));
            serviceLog.setAnomalyPhotos((String) params.get("anomalyPhotos"));
            serviceLog.setAnomalyStatus("REPORTED");
            serviceLogMapper.updateById(serviceLog);
        }
    }

    @Override
    public ServiceLogStatisticsVO getStatistics(String areaId, String providerId, String staffId, String startDate, String endDate) {
        LambdaQueryWrapper<ServiceLog> wrapper = new LambdaQueryWrapper<>();

        ServiceLogStatisticsVO stats = new ServiceLogStatisticsVO();

        // 总服务次数
        Long total = serviceLogMapper.selectCount(wrapper.clone());
        stats.setTotal(total.intValue());

        // 今日新增
        stats.setToday(serviceLogMapper.countToday());

        // 本月新增
        stats.setMonth(serviceLogMapper.countMonth());

        // 审核状态统计
        Integer pendingCount = serviceLogMapper.countPending();
        Integer approvedCount = serviceLogMapper.countApproved();
        Integer rejectedCount = serviceLogMapper.countRejected();
        stats.setPendingCount(pendingCount);
        stats.setApprovedCount(approvedCount);
        stats.setRejectedCount(rejectedCount);

        // 审核通过率
        int totalAudited = approvedCount + rejectedCount;
        if (totalAudited > 0) {
            BigDecimal approvalRate = BigDecimal.valueOf(approvedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAudited), 1, BigDecimal.ROUND_HALF_UP);
            stats.setApprovalRate(approvalRate);
        } else {
            stats.setApprovalRate(BigDecimal.ZERO);
        }

        // 待审核率
        if (total > 0) {
            BigDecimal pendingRate = BigDecimal.valueOf(pendingCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, BigDecimal.ROUND_HALF_UP);
            stats.setPendingRate(pendingRate);
        } else {
            stats.setPendingRate(BigDecimal.ZERO);
        }

        // 平均服务时长
        BigDecimal avgDuration = serviceLogMapper.avgDuration();
        stats.setAvgDuration(avgDuration != null ? avgDuration : BigDecimal.ZERO);

        // 平均服务评分
        BigDecimal avgScore = serviceLogMapper.avgScore();
        stats.setAvgScore(avgScore != null ? avgScore : BigDecimal.ZERO);

        // 异常服务次数
        Integer anomalyCount = serviceLogMapper.countAnomaly();
        stats.setAnomalyCount(anomalyCount);

        // 异常率
        if (total > 0) {
            BigDecimal anomalyRate = BigDecimal.valueOf(anomalyCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, BigDecimal.ROUND_HALF_UP);
            stats.setAnomalyRate(anomalyRate);
        } else {
            stats.setAnomalyRate(BigDecimal.ZERO);
        }

        // 平均审核耗时
        BigDecimal avgReviewTime = serviceLogMapper.avgReviewTimeHours();
        stats.setAvgReviewTime(avgReviewTime != null ? avgReviewTime.setScale(1, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);

        // 服务人员排名
        List<ServiceLogStatisticsVO.StaffRanking> rankings = serviceLogMapper.getStaffRankings();
        if (rankings != null && !rankings.isEmpty()) {
            for (ServiceLogStatisticsVO.StaffRanking r : rankings) {
                int rTotal = r.getApprovedCount() + r.getRejectedCount();
                if (rTotal > 0) {
                    BigDecimal rate = BigDecimal.valueOf(r.getApprovedCount())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(rTotal), 1, BigDecimal.ROUND_HALF_UP);
                    r.setApprovalRate(rate);
                } else {
                    r.setApprovalRate(BigDecimal.ZERO);
                }
            }
        }
        stats.setStaffRankings(rankings);

        return stats;
    }

    private ServiceLogVO convertToVO(ServiceLog serviceLog) {
        if (serviceLog == null) {
            return null;
        }
        ServiceLogVO vo = new ServiceLogVO();
        vo.setId(serviceLog.getServiceLogId());
        vo.setLogNo(serviceLog.getLogNo());
        vo.setOrderId(serviceLog.getOrderId());
        vo.setOrderNo(serviceLog.getOrderNo());
        vo.setElderId(serviceLog.getElderId());
        vo.setElderName(serviceLog.getElderName());
        vo.setElderPhone(serviceLog.getElderPhone());
        vo.setElderAddress(serviceLog.getElderAddress());
        vo.setStaffId(serviceLog.getStaffId());
        vo.setStaffName(serviceLog.getStaffName());
        vo.setStaffPhone(serviceLog.getStaffPhone());
        vo.setProviderId(serviceLog.getProviderId());
        vo.setProviderName(serviceLog.getProviderName());
        vo.setServiceType(serviceLog.getServiceTypeName());
        vo.setServiceCategory(serviceLog.getServiceTypeCode());
        vo.setServiceDate(serviceLog.getServiceDate());
        vo.setServiceStartTime(serviceLog.getServiceStartTime() != null ? serviceLog.getServiceStartTime().toString() : null);
        vo.setServiceEndTime(serviceLog.getServiceEndTime() != null ? serviceLog.getServiceEndTime().toString() : null);
        vo.setServiceDuration(serviceLog.getServiceDuration());
        vo.setStatus(serviceLog.getServiceStatus());
        vo.setHasAnomaly(serviceLog.getAnomalyType() != null);
        vo.setAnomalyType(serviceLog.getAnomalyType());
        vo.setAnomalyDesc(serviceLog.getAnomalyDesc());
        vo.setAnomalyStatus(serviceLog.getAnomalyStatus());
        vo.setReviewRemarks(serviceLog.getReviewRemarks());
        vo.setAuditStatus(serviceLog.getAuditStatus());
        vo.setServiceContent(serviceLog.getServiceComment());
        String photosStr = serviceLog.getServicePhotos();
        if (photosStr != null && !photosStr.isEmpty()) {
            try {
                List<String> photosList = objectMapper.readValue(photosStr,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                vo.setServicePhotos(photosList);
            } catch (Exception e) {
                // If not JSON array format, create single-item list
                vo.setServicePhotos(java.util.Collections.singletonList(photosStr));
            }
        }
        vo.setCreateTime(serviceLog.getCreateTime() != null ? serviceLog.getCreateTime().toString() : null);
        return vo;
    }
}
