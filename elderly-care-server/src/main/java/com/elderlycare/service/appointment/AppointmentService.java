package com.elderlycare.service.appointment;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.appointment.AppointmentCreateDTO;
import com.elderlycare.dto.appointment.AppointmentQueryDTO;
import com.elderlycare.vo.appointment.AppointmentStatisticsVO;
import com.elderlycare.vo.appointment.AppointmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 获取预约列表
     */
    PageResult<AppointmentVO> getAppointmentList(AppointmentQueryDTO query);

    /**
     * 创建预约
     */
    String createAppointment(AppointmentCreateDTO dto);

    /**
     * 获取预约详情
     */
    AppointmentVO getAppointment(String id);

    /**
     * 确认预约
     */
    void confirmAppointment(String id, String providerId, String appointmentTime);

    /**
     * 分配预约
     */
    void assignAppointment(String id, String providerId);

    /**
     * 取消预约
     */
    void cancelAppointment(String id, String reason);

    /**
     * 作废预约
     */
    void invalidateAppointment(String id, String reason);

    /**
     * 导入预约
     */
    Map<String, Object> importAppointment(MultipartFile file);

    /**
     * 生成导入模板Excel
     */
    byte[] generateTemplate();

    /**
     * 获取预约统计
     */
    AppointmentStatisticsVO getStatistics(String areaId, String startDate, String endDate);

    /**
     * 根据手机号获取预约列表
     */
    List<AppointmentVO> getAppointmentsByPhone(String phone);

    /**
     * 获取预约时间轴
     */
    Object getAppointmentTimeline(String id);
}
