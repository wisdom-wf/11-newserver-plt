package com.elderlycare.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.appointment.AppointmentQueryDTO;
import com.elderlycare.entity.appointment.Appointment;
import com.elderlycare.mapper.appointment.AppointmentMapper;
import com.elderlycare.service.appointment.AppointmentService;
import com.elderlycare.vo.appointment.AppointmentStatisticsVO;
import com.elderlycare.vo.appointment.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public void confirmAppointment(String id, String providerId, String appointmentTime) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(id);
        appointment.setProviderId(providerId);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus("CONFIRMED");
        appointment.setConfirmTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void assignAppointment(String id, String providerId) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(id);
        appointment.setProviderId(providerId);
        appointment.setStatus("ASSIGNED");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void cancelAppointment(String id, String reason) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(id);
        appointment.setCancelReason(reason);
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void invalidateAppointment(String id, String reason) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(id);
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
