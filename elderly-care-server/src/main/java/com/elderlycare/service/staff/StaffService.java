package com.elderlycare.service.staff;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.staff.*;
import com.elderlycare.vo.staff.*;

/**
 * 服务人员Service接口
 */
public interface StaffService {

    // ==================== 服务人员管理 ====================

    /**
     * 创建服务人员
     */
    StaffVO createStaff(StaffCreateDTO createDTO);

    /**
     * 分页查询服务人员
     */
    PageResult<StaffVO> queryStaff(StaffQueryDTO queryDTO);

    /**
     * 获取服务人员详情
     */
    StaffVO getStaffById(String staffId);

    /**
     * 更新服务人员
     */
    StaffVO updateStaff(String staffId, StaffUpdateDTO updateDTO);

    /**
     * 删除服务人员
     */
    void deleteStaff(String staffId);

    /**
     * 变更服务人员状态
     */
    void updateStaffStatus(String staffId, StaffStatusDTO statusDTO);

    // ==================== 资质管理 ====================

    /**
     * 添加资质
     */
    QualificationVO addQualification(String staffId, QualificationCreateDTO createDTO);

    /**
     * 获取服务人员资质列表
     */
    java.util.List<QualificationVO> getQualifications(String staffId);

    /**
     * 更新资质
     */
    QualificationVO updateQualification(String qualificationId, QualificationUpdateDTO updateDTO);

    /**
     * 删除资质
     */
    void deleteQualification(String qualificationId);

    // ==================== 排班管理 ====================

    /**
     * 添加排班
     */
    ScheduleVO addSchedule(String staffId, ScheduleCreateDTO createDTO);

    /**
     * 获取服务人员排班列表
     */
    java.util.List<ScheduleVO> getSchedules(String staffId);

    /**
     * 更新排班
     */
    ScheduleVO updateSchedule(String scheduleId, ScheduleUpdateDTO updateDTO);

    /**
     * 删除排班
     */
    void deleteSchedule(String scheduleId);

    /**
     * 查询日期排班
     */
    java.util.List<ScheduleVO> getSchedulesByDate(java.time.LocalDate date);

    // ==================== 签到签退 ====================

    /**
     * 签到
     */
    WorkRecordVO checkIn(CheckInDTO checkInDTO);

    /**
     * 签退
     */
    WorkRecordVO checkOut(CheckOutDTO checkOutDTO);

    /**
     * 查询工作记录
     */
    PageResult<WorkRecordVO> queryWorkRecords(WorkRecordQueryDTO queryDTO);

    // ==================== 服务日志 ====================

    /**
     * 获取服务人员的服务日志列表
     */
    java.util.List<com.elderlycare.vo.servicelog.ServiceLogVO> getServiceLogs(String staffId, int limit);
}
