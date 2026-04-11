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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务日志服务实现
 */
@Service
@RequiredArgsConstructor
public class ServiceLogServiceImpl implements ServiceLogService {

    private final ServiceLogMapper serviceLogMapper;

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
        serviceLog.setServiceStatus(vo.getStatus());
        serviceLogMapper.insert(serviceLog);
    }

    @Override
    public void updateServiceLog(String id, ServiceLogVO vo) {
        // Update not fully implemented
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

        // 今日服务次数
        stats.setToday(0); // TODO: 计算今日服务次数

        // 本月服务次数
        stats.setMonth(0); // TODO: 计算本月服务次数

        // 平均服务时长
        stats.setAvgDuration(BigDecimal.valueOf(120)); // TODO: 计算实际平均值

        // 异常服务次数
        wrapper.eq(ServiceLog::getAnomalyStatus, "REPORTED");
        stats.setAnomalyCount(serviceLogMapper.selectCount(wrapper.clone()).intValue());

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
        vo.setCreateTime(serviceLog.getCreateTime() != null ? serviceLog.getCreateTime().toString() : null);
        return vo;
    }
}
