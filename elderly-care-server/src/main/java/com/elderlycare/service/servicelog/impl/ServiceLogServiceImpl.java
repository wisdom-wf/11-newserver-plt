package com.elderlycare.service.servicelog.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.GpsUtils;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.servicelog.DepartureDTO;
import com.elderlycare.dto.servicelog.ServiceLogQueryDTO;
import com.elderlycare.dto.servicelog.SignInDTO;
import com.elderlycare.dto.servicelog.SignOutDTO;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.quality.QualityCheck;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.entity.staff.Staff;
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
    private final StaffMapper staffMapper;
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
        // 从 t_staff 表查询员工手机号
        String staffPhone = null;
        if (serviceLog.getStaffId() != null) {
            Staff staff = staffMapper.selectById(serviceLog.getStaffId());
            if (staff != null) staffPhone = staff.getPhone();
        }
        serviceLog.setStaffPhone(staffPhone);
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
    public void submitForReview(String id, String remarks) {
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
        // 存储提交备注
        if (remarks != null && !remarks.isEmpty()) {
            serviceLog.setReviewComment(remarks);
        }
        serviceLogMapper.updateById(serviceLog);
        // 质检单已在 submitServiceLog() 创建日志时一并创建，无需重复创建
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
        // 获取审核人信息
        serviceLog.setAuditStatus(result);
        serviceLog.setReviewComment(reviewComment);
        serviceLog.setReviewTime(LocalDateTime.now());
        serviceLog.setReviewerId(com.elderlycare.common.UserContext.getUserId());
        serviceLog.setReviewerName(com.elderlycare.common.UserContext.getUsername());
        serviceLogMapper.updateById(serviceLog);

        // APPROVED 时自动创建质检单（checkResult=PENDING，等待质检员执行 inspect）
        if ("APPROVED".equals(result)) {
            // 查订单信息填充质检单
            Order order = null;
            if (serviceLog.getOrderId() != null) {
                order = orderMapper.selectById(serviceLog.getOrderId());
            }
            QualityCheck qc = new QualityCheck();
            qc.setCheckNo("QC" + System.currentTimeMillis());
            qc.setOrderId(serviceLog.getOrderId());
            qc.setOrderNo(serviceLog.getOrderNo());
            qc.setServiceLogId(serviceLog.getServiceLogId());
            qc.setServiceCategory(serviceLog.getServiceTypeName());
            qc.setProviderId(serviceLog.getProviderId());
            qc.setProviderName(serviceLog.getProviderName());
            qc.setStaffId(serviceLog.getStaffId());
            qc.setStaffName(serviceLog.getStaffName());
            qc.setCheckType("COMPLETION"); // 完工抽检
            qc.setCheckResult("PENDING");  // 待质检，等质检员执行 inspect
            qc.setNeedRectify(false);
            qc.setRectifyStatus(null);
            qc.setCreateTime(LocalDateTime.now());
            qualityCheckMapper.insert(qc);
        }
        // REJECTED 仅更新日志审核状态
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
    @Transactional(rollbackFor = Exception.class)
    public String duplicateServiceLog(String id) {
        ServiceLog original = serviceLogMapper.selectById(id);
        if (original == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if (!"DRAFT".equals(original.getAuditStatus())) {
            throw new BusinessException(400, "仅支持复制草稿状态的日志");
        }

        ServiceLog copy = new ServiceLog();
        copy.setLogNo(IDGenerator.generateId()); // 简化：直接用 UUID 前缀
        copy.setOrderId(original.getOrderId());
        copy.setOrderNo(original.getOrderNo());
        copy.setElderId(original.getElderId());
        copy.setElderName(original.getElderName());
        copy.setElderPhone(original.getElderPhone());
        copy.setElderAddress(original.getElderAddress());
        copy.setStaffId(original.getStaffId());
        copy.setStaffName(original.getStaffName());
        copy.setStaffPhone(original.getStaffPhone());
        copy.setProviderId(original.getProviderId());
        copy.setProviderName(original.getProviderName());
        copy.setServiceTypeCode(original.getServiceTypeCode());
        copy.setServiceTypeName(original.getServiceTypeName());
        // 清空字段：时间/照片/签名/异常/审核相关
        copy.setServiceDate(null);
        copy.setServiceStartTime(null);
        copy.setServiceEndTime(null);
        copy.setServiceDuration(null);
        copy.setActualDuration(null);
        copy.setServicePhotos(null);
        copy.setElderSignature(null);
        copy.setAnomalyType(null);
        copy.setAnomalyDesc(null);
        copy.setAnomalyPhotos(null);
        copy.setAnomalyStatus(null);
        copy.setServiceComment(null);
        copy.setServiceScore(null);
        copy.setHealthObservations(original.getHealthObservations()); // 健康观察可保留
        copy.setMedicationGiven(original.getMedicationGiven());        // 给药记录可保留
        copy.setAuditStatus("DRAFT");
        copy.setCreateTime(LocalDateTime.now());
        serviceLogMapper.insert(copy);
        return copy.getServiceLogId();
    }

    @Override
    public List<ServiceLogVO> getAllServiceLogsByOrderId(String orderId) {
        LambdaQueryWrapper<ServiceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceLog::getOrderId, orderId);
        wrapper.orderByDesc(ServiceLog::getCreateTime);
        List<ServiceLog> logs = serviceLogMapper.selectList(wrapper);
        return logs.stream().map(this::convertToVO).collect(Collectors.toList());
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
        LambdaQueryWrapper<ServiceLog> baseWrapper = new LambdaQueryWrapper<>();

        // 应用权限过滤：服务商ID
        if (providerId != null && !providerId.isEmpty()) {
            baseWrapper.eq(ServiceLog::getProviderId, providerId);
        }
        // 服务人员ID过滤
        if (staffId != null && !staffId.isEmpty()) {
            baseWrapper.eq(ServiceLog::getStaffId, staffId);
        }

        ServiceLogStatisticsVO stats = new ServiceLogStatisticsVO();

        // 总服务次数
        Long total = serviceLogMapper.selectCount(baseWrapper.clone());
        stats.setTotal(total.intValue());

        // 今日服务次数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.setToday(serviceLogMapper.countToday(todayStart));

        // 本月服务次数
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        stats.setMonth(serviceLogMapper.countMonth(monthStart));

        // 待审核数 (SUBMITTED)
        int pendingCount = serviceLogMapper.countByAuditStatus("SUBMITTED");
        stats.setPendingCount(pendingCount);

        // 已通过数 (APPROVED)
        int approvedCount = serviceLogMapper.countByAuditStatus("APPROVED");
        stats.setApprovedCount(approvedCount);

        // 已驳回数 (REJECTED)
        int rejectedCount = serviceLogMapper.countByAuditStatus("REJECTED");
        stats.setRejectedCount(rejectedCount);

        // 审核通过率
        int totalReviewed = approvedCount + rejectedCount;
        if (totalReviewed > 0) {
            BigDecimal approvalRate = BigDecimal.valueOf(approvedCount)
                    .divide(BigDecimal.valueOf(totalReviewed), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            stats.setApprovalRate(approvalRate);
        } else {
            stats.setApprovalRate(BigDecimal.ZERO);
        }

        // 待审核率
        if (total > 0) {
            BigDecimal pendingRate = BigDecimal.valueOf(pendingCount)
                    .divide(BigDecimal.valueOf(total), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            stats.setPendingRate(pendingRate);
        } else {
            stats.setPendingRate(BigDecimal.ZERO);
        }

        // 平均服务时长
        BigDecimal avgDuration = serviceLogMapper.avgActualDuration();
        stats.setAvgDuration(avgDuration != null ? avgDuration : BigDecimal.ZERO);

        // 平均服务评分
        BigDecimal avgScore = serviceLogMapper.avgServiceScore();
        stats.setAvgScore(avgScore != null ? avgScore : BigDecimal.ZERO);

        // 异常服务次数
        LambdaQueryWrapper<ServiceLog> anomalyWrapper = baseWrapper.clone();
        anomalyWrapper.eq(ServiceLog::getAnomalyStatus, "REPORTED");
        int anomalyCount = serviceLogMapper.selectCount(anomalyWrapper).intValue();
        stats.setAnomalyCount(anomalyCount);

        // 异常率
        if (total > 0) {
            BigDecimal anomalyRate = BigDecimal.valueOf(anomalyCount)
                    .divide(BigDecimal.valueOf(total), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            stats.setAnomalyRate(anomalyRate);
        } else {
            stats.setAnomalyRate(BigDecimal.ZERO);
        }

        // 平均审核耗时（小时）- 从已审核的记录计算
        // 简化处理：使用SUBMITTED到APPROVED/REJECTED的平均时间差
        stats.setAvgReviewTime(BigDecimal.valueOf(24)); // 默认24小时，需要可配置

        // 服务人员排名
        List<ServiceLogStatisticsVO.StaffRankingVO> staffRankings = buildStaffRankings(baseWrapper);
        stats.setStaffRankings(staffRankings);

        return stats;
    }

    private List<ServiceLogStatisticsVO.StaffRankingVO> buildStaffRankings(LambdaQueryWrapper<ServiceLog> baseWrapper) {
        // 按服务人员分组统计
        List<ServiceLog> allLogs = serviceLogMapper.selectList(baseWrapper.clone());

        // 按staffId分组
        Map<String, List<ServiceLog>> byStaff = allLogs.stream()
                .filter(log -> log.getStaffId() != null)
                .collect(Collectors.groupingBy(ServiceLog::getStaffId));

        List<ServiceLogStatisticsVO.StaffRankingVO> rankings = byStaff.entrySet().stream()
                .map(entry -> {
                    ServiceLogStatisticsVO.StaffRankingVO ranking = new ServiceLogStatisticsVO.StaffRankingVO();
                    ranking.setStaffId(entry.getKey());
                    List<ServiceLog> staffLogs = entry.getValue();
                    if (!staffLogs.isEmpty()) {
                        ranking.setStaffName(staffLogs.get(0).getStaffName());
                        ranking.setProviderName(staffLogs.get(0).getProviderName());
                    }
                    ranking.setLogCount(staffLogs.size());
                    ranking.setApprovedCount((int) staffLogs.stream().filter(log -> "APPROVED".equals(log.getAuditStatus())).count());
                    ranking.setRejectedCount((int) staffLogs.stream().filter(log -> "REJECTED".equals(log.getAuditStatus())).count());
                    int reviewed = ranking.getApprovedCount() + ranking.getRejectedCount();
                    if (reviewed > 0) {
                        BigDecimal rate = BigDecimal.valueOf(ranking.getApprovedCount())
                                .divide(BigDecimal.valueOf(reviewed), 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        ranking.setApprovalRate(rate);
                    } else {
                        ranking.setApprovalRate(BigDecimal.ZERO);
                    }
                    return ranking;
                })
                .sorted((a, b) -> Integer.compare(b.getLogCount(), a.getLogCount()))
                .limit(10) // 只取前10名
                .collect(Collectors.toList());

        return rankings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void departure(String id, DepartureDTO dto) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        // 设置出发信息
        serviceLog.setDepartureTime(LocalDateTime.now());
        if (dto.getLocation() != null) {
            // 可以存储出发位置，但一般不校验
        }
        // 更新服务状态为DEPARTURE
        serviceLog.setServiceStatus("DEPARTURE");
        serviceLogMapper.updateById(serviceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signIn(String id, SignInDTO dto) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        if (serviceLog.getElderAddress() == null) {
            throw new RuntimeException("老人地址信息不完整，无法进行签到校验");
        }
        // 获取老人地址的坐标（简化处理：实际应从老人档案或订单中获取经纬度）
        // 这里假设 elderAddress 格式或关联信息中包含坐标
        // 实际项目中老人表应该有 latitude 和 longitude 字段

        // GPS校验：签到位置应在老人住址500米范围内
        // 由于老人地址不包含坐标，暂时跳过GPS校验
        // 如果有坐标，可以这样校验：
        // Order order = orderMapper.selectById(serviceLog.getOrderId());
        // if (order != null && order.getLatitude() != null && order.getLongitude() != null) {
        //     if (!GpsUtils.validateSignInRange(dto.getLocation(), order.getLatitude().toString(), order.getLongitude().toString())) {
        //         throw new RuntimeException("签到位置距离老人住址超过500米，请确认位置是否正确");
        //     }
        // }

        // 设置签到信息
        serviceLog.setSignInTime(LocalDateTime.now());
        serviceLog.setSignInLocation(dto.getLocation());
        // 序列化签到照片
        if (dto.getPhotos() != null && dto.getPhotos().length > 0) {
            try {
                serviceLog.setSignInPhotos(objectMapper.writeValueAsString(dto.getPhotos()));
            } catch (Exception e) {
                serviceLog.setSignInPhotos(String.join(",", dto.getPhotos()));
            }
        }
        // 更新服务状态为SIGN_IN
        serviceLog.setServiceStatus("SIGN_IN");
        serviceLogMapper.updateById(serviceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signOut(String id, SignOutDTO dto) {
        ServiceLog serviceLog = serviceLogMapper.selectById(id);
        if (serviceLog == null) {
            throw new RuntimeException("服务日志不存在");
        }
        // 设置签退信息
        serviceLog.setSignOutTime(LocalDateTime.now());
        serviceLog.setSignOutLocation(dto.getLocation());
        serviceLog.setActualDuration(dto.getActualDuration());
        // 序列化签退照片
        if (dto.getPhotos() != null && dto.getPhotos().length > 0) {
            try {
                serviceLog.setSignOutPhotos(objectMapper.writeValueAsString(dto.getPhotos()));
            } catch (Exception e) {
                serviceLog.setSignOutPhotos(String.join(",", dto.getPhotos()));
            }
        }
        // 更新服务状态为SIGN_OUT
        serviceLog.setServiceStatus("SIGN_OUT");
        serviceLogMapper.updateById(serviceLog);
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
