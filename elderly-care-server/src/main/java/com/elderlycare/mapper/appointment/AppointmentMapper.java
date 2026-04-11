package com.elderlycare.mapper.appointment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.appointment.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约Mapper
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
