package com.elderlycare.service.appointment;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.appointment.AppointmentCreateDTO;
import com.elderlycare.dto.appointment.AppointmentQueryDTO;
import com.elderlycare.dto.appointment.PublicAppointmentSubmitDTO;
import com.elderlycare.dto.appointment.AppointmentUpdateDTO;
import com.elderlycare.vo.appointment.AppointmentStatisticsVO;
import com.elderlycare.vo.appointment.AppointmentVO;
import com.elderlycare.vo.appointment.PublicAppointmentVO;
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
    AppointmentStatisticsVO getStatistics(String providerId, String areaId, String startDate, String endDate);

    /**
     * 根据手机号获取预约列表
     */
    List<AppointmentVO> getAppointmentsByPhone(String phone);

    /**
     * 获取预约时间轴
     */
    Object getAppointmentTimeline(String id);

    /**
     * 批量删除预约
     */
    void batchDeleteAppointment(List<String> ids);

    // ========== 预约二维码（公开预约） ==========

    /**
     * 生成预约二维码Token（超级管理员）
     */
    String generateAppointmentToken();

    /**
     * 校验预约Token有效性
     */
    PublicAppointmentVO validateAppointmentToken(String token);

    /**
     * 提交公开预约（扫码填写）
     */
    String submitPublicAppointment(String token, PublicAppointmentSubmitDTO dto);

    /**
     * 获取二维码图片字节数组
     */
    byte[] getQRCodeImage(String token) throws Exception;

    /**
     * 停用预约二维码Token
     */
    void disableAppointmentToken(String token);

    /**
     * 编辑预约业务信息（服务类型/预约时间/备注）
     * 仅允许 PENDING 状态
     */
    void updateAppointmentInfo(String id, AppointmentUpdateDTO dto);
}
