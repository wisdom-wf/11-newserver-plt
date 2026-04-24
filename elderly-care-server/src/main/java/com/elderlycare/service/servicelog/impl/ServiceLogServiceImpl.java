package com.elderlycare.service.servicelog.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.servicelog.ServiceLogQueryDTO;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.quality.QualityCheck;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.service.servicelog.ServiceLogService;
import java.util.List;
import com.elderlycare.vo.servicelog.ServiceLogStatisticsVO;
import com.elderlycare.vo.servicelog.ServiceLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 服务日志服务实现
 */
@Service
@RequiredArgsConstructor
public class ServiceLogServiceImpl implements ServiceLogService {

    private final ServiceLogMapper serviceLogMapper;
    private final QualityCheckMapper qualityCheckMapper;
    private final OrderMapper orderMapper;
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
        if (query.getAuditStatus() != null && !query.getAuditStatus().isEmpty()) {
            wrapper.eq(ServiceLog::getAuditStatus, query.getAuditStatus());
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
        List<ServiceLog> list = serviceLogMapper.selectList(
            new LambdaQueryWrapper<ServiceLog>()
                .eq(ServiceLog::getOrderId, orderId)
                .orderByDesc(ServiceLog::getCreateTime)
                .last("LIMIT 1")
        );
        if (list == null || list.isEmpty()) {
            return null;
        }
        return convertToVO(list.get(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitServiceLog(ServiceLogVO vo) {
        // 先获取订单信息，填充服务商和人员字段
        Order order = orderMapper.selectByIdWithNames(vo.getOrderId());

        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setLogNo("SL" + System.currentTimeMillis());
        serviceLog.setOrderId(vo.getOrderId());
        // 优先使用VO中的值，否则使用订单中的值
        serviceLog.setElderId(vo.getElderId() != null ? vo.getElderId() : (order != null ? order.getElderId() : null));
        serviceLog.setElderName(vo.getElderName() != null ? vo.getElderName() : (order != null ? order.getElderName() : null));
        serviceLog.setElderPhone(vo.getElderPhone() != null ? vo.getElderPhone() : (order != null ? order.getElderPhone() : null));
        serviceLog.setStaffId(vo.getStaffId() != null ? vo.getStaffId() : (order != null ? order.getStaffId() : null));
        serviceLog.setStaffName(vo.getStaffName() != null ? vo.getStaffName() : (order != null ? order.getStaffName() : null));
        serviceLog.setStaffPhone(order != null ? order.getStaffPhone() : null);
        serviceLog.setProviderId(vo.getProviderId() != null ? vo.getProviderId() : (order != null ? order.getProviderId() : null));
        serviceLog.setProviderName(order != null ? order.getProviderName() : null);
        serviceLog.setElderAddress(order != null ? order.getServiceAddress() : null);
        serviceLog.setOrderNo(order != null ? order.getOrderNo() : null);
        serviceLog.setServiceTypeCode(vo.getServiceCategory() != null ? vo.getServiceCategory() : (order != null ? order.getServiceTypeCode() : null));
        serviceLog.setServiceTypeName(vo.getServiceType() != null ? vo.getServiceType() : (order != null ? order.getServiceTypeName() : null));
        serviceLog.setServiceDate(vo.getServiceDate());
        serviceLog.setServiceStartTime(vo.getServiceStartTime() != null ? LocalDateTime.parse(vo.getServiceStartTime()) : null);
        serviceLog.setServiceEndTime(vo.getServiceEndTime() != null ? LocalDateTime.parse(vo.getServiceEndTime()) : null);
        serviceLog.setServiceDuration(vo.getServiceDuration());
        serviceLog.setServiceComment(vo.getServiceContent() != null ? vo.getServiceContent() : vo.getServiceComment());
        serviceLog.setServiceStatus(vo.getStatus() != null ? vo.getStatus() : "PENDING");
        // 将照片数组序列化为JSON字符串存储
        if (vo.getServicePhotos() != null && vo.getServicePhotos().length > 0) {
            try {
                serviceLog.setServicePhotos(objectMapper.writeValueAsString(vo.getServicePhotos()));
            } catch (Exception e) {
                serviceLog.setServicePhotos(String.join(",", vo.getServicePhotos()));
            }
        }
        serviceLog.setAuditStatus("DRAFT");
        serviceLog.setCreateTime(LocalDateTime.now());
        // 健康观察字段
        serviceLog.setHealthObservations(vo.getHealthObservations());
        serviceLog.setMedicationGiven(vo.getMedicationGiven());
        serviceLogMapper.insert(serviceLog);

        // 自动生成质检单（待质检状态）
        QualityCheck qualityCheck = new QualityCheck();
        qualityCheck.setQualityCheckId(IDGenerator.generateId());
        qualityCheck.setCheckNo("QC" + System.currentTimeMillis());
        qualityCheck.setOrderId(vo.getOrderId());
        qualityCheck.setOrderNo(order != null ? order.getOrderNo() : null);
        qualityCheck.setServiceLogId(serviceLog.getServiceLogId());
        qualityCheck.setServiceCategory(serviceLog.getServiceTypeCode());
        qualityCheck.setProviderId(serviceLog.getProviderId());
        qualityCheck.setProviderName(serviceLog.getProviderName());
        qualityCheck.setStaffId(serviceLog.getStaffId());
        qualityCheck.setStaffName(serviceLog.getStaffName());
        qualityCheck.setCheckType("COMPLETION"); // 完工质检
        qualityCheck.setCheckMethod("PHOTO_REVIEW"); // 默认照片审核
        qualityCheck.setCheckResult("PENDING"); // 待质检
        qualityCheck.setRectifyStatus("PENDING");
        qualityCheck.setCreateTime(LocalDateTime.now());
        qualityCheckMapper.insert(qualityCheck);

        return serviceLog.getServiceLogId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceLog(String id, ServiceLogVO vo) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        // 只有草稿状态才能更新
        if (!"DRAFT".equals(serviceLog.getAuditStatus())) {
            throw new RuntimeException("只有草稿状态的服务日志才能修改");
        }
        serviceLog.setServiceDate(vo.getServiceDate());
        serviceLog.setServiceStartTime(vo.getServiceStartTime() != null ? LocalDateTime.parse(vo.getServiceStartTime()) : null);
        serviceLog.setServiceEndTime(vo.getServiceEndTime() != null ? LocalDateTime.parse(vo.getServiceEndTime()) : null);
        serviceLog.setServiceDuration(vo.getServiceDuration());
        serviceLog.setServiceComment(vo.getServiceContent() != null ? vo.getServiceContent() : vo.getServiceComment());
        // 将照片数组序列化为JSON字符串存储
        if (vo.getServicePhotos() != null && vo.getServicePhotos().length > 0) {
            try {
                serviceLog.setServicePhotos(objectMapper.writeValueAsString(vo.getServicePhotos()));
            } catch (Exception e) {
                serviceLog.setServicePhotos(String.join(",", vo.getServicePhotos()));
            }
        }
        serviceLog.setActualDuration(vo.getActualDuration());
        serviceLog.setHealthObservations(vo.getHealthObservations());
        serviceLog.setMedicationGiven(vo.getMedicationGiven());
        serviceLogMapper.updateById(serviceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(String id) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if (!"DRAFT".equals(serviceLog.getAuditStatus())) {
            throw new RuntimeException("只有草稿状态才能提交审核");
        }
        // 校验必填字段
        if (serviceLog.getServiceDate() == null || serviceLog.getServiceDate().isEmpty()) {
            throw new RuntimeException("请填写服务日期");
        }
        if (serviceLog.getServiceStartTime() == null) {
            throw new RuntimeException("请填写服务开始时间");
        }
        if (serviceLog.getServiceEndTime() == null) {
            throw new RuntimeException("请填写服务结束时间");
        }
        if (serviceLog.getServicePhotos() == null || serviceLog.getServicePhotos().isEmpty()) {
            throw new RuntimeException("请上传服务照片");
        }
        serviceLog.setAuditStatus("SUBMITTED");
        serviceLogMapper.updateById(serviceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewServiceLog(String id, String result, String reviewComment) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if (!"SUBMITTED".equals(serviceLog.getAuditStatus())) {
            throw new RuntimeException("只有已提交状态才能审核");
        }
        if (result == null || (!"APPROVED".equals(result) && !"REJECTED".equals(result))) {
            throw new RuntimeException("审核结果只能是APPROVED或REJECTED");
        }
        // 获取审核人信息（简化处理，实际应从UserContext获取）
        serviceLog.setAuditStatus(result);
        serviceLog.setReviewComment(reviewComment);
        serviceLog.setReviewTime(LocalDateTime.now());
        serviceLogMapper.updateById(serviceLog);

        // 更新关联质检单状态
        LambdaQueryWrapper<com.elderlycare.entity.quality.QualityCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.elderlycare.entity.quality.QualityCheck::getServiceLogId, id);
        com.elderlycare.entity.quality.QualityCheck qualityCheck = qualityCheckMapper.selectOne(wrapper);
        if (qualityCheck != null) {
            qualityCheck.setCheckResult("APPROVED".equals(result) ? "QUALIFIED" : "UNQUALIFIED");
            qualityCheck.setCheckRemark(reviewComment);
            qualityCheck.setCheckTime(LocalDateTime.now());
            if ("REJECTED".equals(result)) {
                qualityCheck.setNeedRectify(true);
                qualityCheck.setRectifyStatus("PENDING");
            }
            qualityCheckMapper.updateById(qualityCheck);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceLog(String id) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if (!"DRAFT".equals(serviceLog.getAuditStatus())) {
            throw new RuntimeException("只有草稿状态才能删除");
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

        // 今日服务次数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.setToday(serviceLogMapper.countToday(todayStart));

        // 本月服务次数
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        stats.setMonth(serviceLogMapper.countMonth(monthStart));

        // 平均服务时长
        BigDecimal avgDuration = serviceLogMapper.avgActualDuration();
        stats.setAvgDuration(avgDuration != null ? avgDuration : BigDecimal.ZERO);

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
        vo.setServiceLogId(serviceLog.getServiceLogId());
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
        vo.setActualDuration(serviceLog.getActualDuration());
        vo.setServiceComment(serviceLog.getServiceComment());
        vo.setServiceContent(serviceLog.getServiceComment()); // 前端字段名映射
        vo.setStatus(serviceLog.getServiceStatus());
        vo.setHasAnomaly(serviceLog.getAnomalyType() != null);
        vo.setAnomalyType(serviceLog.getAnomalyType());
        vo.setAnomalyDesc(serviceLog.getAnomalyDesc());
        vo.setAnomalyStatus(serviceLog.getAnomalyStatus());
        vo.setCreateTime(serviceLog.getCreateTime() != null ? serviceLog.getCreateTime().toString() : null);
        // 审核相关字段
        vo.setAuditStatus(serviceLog.getAuditStatus());
        vo.setReviewComment(serviceLog.getReviewComment());
        vo.setReviewerId(serviceLog.getReviewerId());
        vo.setReviewerName(serviceLog.getReviewerName());
        vo.setReviewTime(serviceLog.getReviewTime() != null ? serviceLog.getReviewTime().toString() : null);
        // 健康观察字段
        vo.setHealthObservations(serviceLog.getHealthObservations());
        vo.setMedicationGiven(serviceLog.getMedicationGiven());
        // 解析服务照片JSON，设置为servicePhotos数组供前端使用
        if (serviceLog.getServicePhotos() != null && !serviceLog.getServicePhotos().isEmpty()) {
            try {
                vo.setServicePhotos(objectMapper.readValue(serviceLog.getServicePhotos(), String[].class));
            } catch (Exception e) {
                vo.setServicePhotos(new String[]{serviceLog.getServicePhotos()});
            }
        }
        return vo;
    }
}
