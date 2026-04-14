package com.elderlycare.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.appointment.AppointmentQueryDTO;
import com.elderlycare.entity.appointment.Appointment;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.order.OrderStatus;
import com.elderlycare.mapper.appointment.AppointmentMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.appointment.AppointmentService;
import com.elderlycare.vo.appointment.AppointmentStatisticsVO;
import com.elderlycare.vo.appointment.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约服务实现
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final OrderMapper orderMapper;

    @Override
    public PageResult<AppointmentVO> getAppointmentList(AppointmentQueryDTO query) {
        Page<Appointment> page = new Page<>(query.getCurrent(), query.getPageSize());
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();

        if (query.getAppointmentNo() != null && !query.getAppointmentNo().isEmpty()) {
            wrapper.eq(Appointment::getAppointmentNo, query.getAppointmentNo());
        }
        if (query.getElderName() != null && !query.getElderName().isEmpty()) {
            wrapper.like(Appointment::getElderName, query.getElderName());
        }
        if (query.getElderPhone() != null && !query.getElderPhone().isEmpty()) {
            wrapper.eq(Appointment::getElderPhone, query.getElderPhone());
        }
        if (query.getServiceTypeCode() != null && !query.getServiceTypeCode().isEmpty()) {
            wrapper.eq(Appointment::getServiceTypeCode, query.getServiceTypeCode());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Appointment::getStatus, query.getStatus());
        }
        if (query.getProviderId() != null && !query.getProviderId().isEmpty()) {
            wrapper.eq(Appointment::getProviderId, query.getProviderId());
        }

        wrapper.orderByDesc(Appointment::getCreateTime);

        IPage<Appointment> result = appointmentMapper.selectPage(page, wrapper);

        List<AppointmentVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), voList);
    }

    @Override
    public AppointmentVO getAppointment(String id) {
        Appointment appointment = appointmentMapper.selectById(id);
        return convertToVO(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(String id, String providerId, String appointmentTime) {
        // 1. 先查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有PENDING状态可以确认
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("只有待确认状态的预约可以进行确认操作");
        }

        // 3. 参数校验
        if (providerId == null || providerId.isEmpty()) {
            throw new RuntimeException("请选择服务商");
        }
        if (appointmentTime == null || appointmentTime.isEmpty()) {
            throw new RuntimeException("请填写预约时间");
        }

        // 4. 更新预约状态
        appointment.setProviderId(providerId);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus("CONFIRMED");
        appointment.setConfirmTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        // 5. 注意：订单创建在分配服务人员时进行，此处仅确认预约
    }

    /**
     * 根据预约创建订单
     */
    private void createOrderFromAppointment(Appointment appointment) {
        // 生成订单编号
        String orderNo = generateOrderNo();

        // 解析预约时间为日期和时间
        String appointmentTime = appointment.getAppointmentTime();
        LocalDate serviceDate = LocalDate.now(); // 默认今天
        String serviceTime = "09:00:00"; // 默认时间

        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            try {
                // 尝试解析 "2024-04-15 10:00:00" 格式
                if (appointmentTime.contains(" ")) {
                    String[] parts = appointmentTime.split(" ");
                    if (parts.length >= 1) {
                        serviceDate = LocalDate.parse(parts[0]);
                    }
                    if (parts.length >= 2) {
                        serviceTime = parts[1];
                    }
                } else {
                    serviceDate = LocalDate.parse(appointmentTime);
                }
            } catch (Exception e) {
                // 解析失败，使用默认值
            }
        }

        // 创建订单
        Order order = new Order();
        order.setOrderId(IDGenerator.generateId());
        order.setOrderNo(orderNo);
        order.setElderName(appointment.getElderName());
        order.setElderPhone(appointment.getElderPhone());
        order.setServiceTypeCode(appointment.getServiceTypeCode());
        order.setServiceTypeName(appointment.getServiceType());
        order.setServiceDate(serviceDate);
        order.setServiceTime(serviceTime);
        order.setServiceDuration(appointment.getServiceDuration());
        order.setServiceAddress(appointment.getElderAddress());
        order.setOrderType("NORMAL");
        order.setOrderSource("APPOINTMENT"); // 标记来源为预约
        order.setSubsidyType("SELF_PAY"); // 默认自费
        order.setStatus(OrderStatus.CREATED.getCode());
        order.setProviderId(appointment.getProviderId());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String prefix = "ORD";
        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + date + random;
    }

    @Override
    public void assignAppointment(String id, String providerId) {
        // 1. 查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有CONFIRMED状态可以分配
        if (!"CONFIRMED".equals(appointment.getStatus())) {
            throw new RuntimeException("只有已确认状态的预约可以进行分配");
        }

        // 3. 参数校验
        if (providerId == null || providerId.isEmpty()) {
            throw new RuntimeException("请选择服务商");
        }

        // 4. 执行分配
        appointment.setProviderId(providerId);
        appointment.setStatus("ASSIGNED");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void cancelAppointment(String id, String reason) {
        // 1. 查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有PENDING状态可以取消
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("只有待确认状态的预约可以取消");
        }

        // 3. 执行取消
        appointment.setCancelReason(reason);
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void invalidateAppointment(String id, String reason) {
        // 1. 查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有PENDING状态可以作废
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("只有待确认状态的预约可以作废");
        }

        // 3. 执行作废
        appointment.setCancelReason(reason);
        appointment.setStatus("INVALID");
        appointment.setValidity("INVALID");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public Map<String, Object> importAppointment(MultipartFile file) {
        // TODO: 实现Excel导入逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", 0);
        result.put("failCount", 0);
        result.put("errors", List.of());
        return result;
    }

    @Override
    public String getTemplateUrl() {
        // TODO: 返回模板下载地址
        return "/api/appointment/template/download";
    }

    @Override
    public AppointmentStatisticsVO getStatistics(String areaId, String startDate, String endDate) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();

        AppointmentStatisticsVO stats = new AppointmentStatisticsVO();

        // 总数
        stats.setTotal(appointmentMapper.selectCount(wrapper.clone()).intValue());

        // 待处理
        wrapper.eq(Appointment::getStatus, "PENDING");
        stats.setPending(appointmentMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 已确认
        wrapper.eq(Appointment::getStatus, "CONFIRMED");
        stats.setConfirmed(appointmentMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 已分配
        wrapper.eq(Appointment::getStatus, "ASSIGNED");
        stats.setAssigned(appointmentMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 已完成
        wrapper.eq(Appointment::getStatus, "COMPLETED");
        stats.setCompleted(appointmentMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 已取消
        wrapper.eq(Appointment::getStatus, "CANCELLED");
        stats.setCancelled(appointmentMapper.selectCount(wrapper.clone()).intValue());
        wrapper.clear();

        // 已作废
        wrapper.eq(Appointment::getStatus, "INVALID");
        stats.setInvalid(appointmentMapper.selectCount(wrapper.clone()).intValue());

        return stats;
    }

    @Override
    public List<AppointmentVO> getAppointmentsByPhone(String phone) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getElderPhone, phone);
        wrapper.orderByDesc(Appointment::getCreateTime);

        List<Appointment> list = appointmentMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private AppointmentVO convertToVO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentVO vo = new AppointmentVO();
        vo.setAppointmentId(appointment.getAppointmentId());
        vo.setId(appointment.getAppointmentId());
        vo.setAppointmentNo(appointment.getAppointmentNo());
        vo.setElderName(appointment.getElderName());
        vo.setElderIdCard(appointment.getElderIdCard());
        vo.setElderPhone(appointment.getElderPhone());
        vo.setElderAddress(appointment.getElderAddress());
        vo.setElderAreaId(appointment.getElderAreaId());
        vo.setElderAreaName(appointment.getElderAreaName());
        vo.setServiceType(appointment.getServiceType());
        vo.setServiceTypeCode(appointment.getServiceTypeCode());
        vo.setServiceContent(appointment.getServiceContent());
        vo.setAppointmentTime(appointment.getAppointmentTime());
        vo.setServiceDuration(appointment.getServiceDuration());
        vo.setProviderId(appointment.getProviderId());
        vo.setProviderName(appointment.getProviderName());
        vo.setProviderAddress(appointment.getProviderAddress());
        vo.setVisitorCount(appointment.getVisitorCount());
        vo.setRemark(appointment.getRemark());
        vo.setStatus(appointment.getStatus());
        vo.setValidity(appointment.getValidity());
        vo.setCancelReason(appointment.getCancelReason());
        vo.setReplyInfo(appointment.getReplyInfo());
        vo.setAssessmentType(appointment.getAssessmentType());
        vo.setCreateTime(appointment.getCreateTime() != null ? appointment.getCreateTime().toString() : null);
        vo.setConfirmTime(appointment.getConfirmTime() != null ? appointment.getConfirmTime().toString() : null);
        vo.setUpdateTime(appointment.getUpdateTime() != null ? appointment.getUpdateTime().toString() : null);
        return vo;
    }
}
