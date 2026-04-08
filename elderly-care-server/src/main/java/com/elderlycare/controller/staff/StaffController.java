package com.elderlycare.controller.staff;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.staff.*;
import com.elderlycare.service.staff.StaffService;
import com.elderlycare.vo.staff.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 服务人员Controller
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    // ==================== 服务人员管理 ====================

    /**
     * 创建服务人员
     */
    @PostMapping("")
    public Result<StaffVO> createStaff(@RequestBody StaffCreateDTO createDTO) {
        StaffVO vo = staffService.createStaff(createDTO);
        return Result.success(vo);
    }

    /**
     * 分页查询服务人员
     */
    @GetMapping("")
    public Result<PageResult<StaffVO>> queryStaff(
            @RequestParam(required = false) String staffName,
            @RequestParam(required = false) String staffNo,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String providerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        StaffQueryDTO queryDTO = new StaffQueryDTO();
        queryDTO.setStaffName(staffName);
        queryDTO.setStaffNo(staffNo);
        queryDTO.setPhone(phone);
        queryDTO.setStatus(status);
        queryDTO.setProviderId(providerId);
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        PageResult<StaffVO> result = staffService.queryStaff(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取服务人员详情
     */
    @GetMapping("/{staffId}")
    public Result<StaffVO> getStaffById(@PathVariable String staffId) {
        StaffVO vo = staffService.getStaffById(staffId);
        return Result.success(vo);
    }

    /**
     * 更新服务人员
     */
    @PutMapping("/{staffId}")
    public Result<StaffVO> updateStaff(
            @PathVariable String staffId,
            @RequestBody StaffUpdateDTO updateDTO) {
        StaffVO vo = staffService.updateStaff(staffId, updateDTO);
        return Result.success(vo);
    }

    /**
     * 删除服务人员
     */
    @DeleteMapping("/{staffId}")
    public Result<Void> deleteStaff(@PathVariable String staffId) {
        staffService.deleteStaff(staffId);
        return Result.success();
    }

    /**
     * 变更服务人员状态
     */
    @PutMapping("/{staffId}/status")
    public Result<Void> updateStaffStatus(
            @PathVariable String staffId,
            @RequestBody StaffStatusDTO statusDTO) {
        staffService.updateStaffStatus(staffId, statusDTO);
        return Result.success();
    }

    // ==================== 资质管理 ====================

    /**
     * 添加资质
     */
    @PostMapping("/{staffId}/qualifications")
    public Result<QualificationVO> addQualification(
            @PathVariable String staffId,
            @RequestBody QualificationCreateDTO createDTO) {
        QualificationVO vo = staffService.addQualification(staffId, createDTO);
        return Result.success(vo);
    }

    /**
     * 获取服务人员资质列表
     */
    @GetMapping("/{staffId}/qualifications")
    public Result<List<QualificationVO>> getQualifications(@PathVariable String staffId) {
        List<QualificationVO> list = staffService.getQualifications(staffId);
        return Result.success(list);
    }

    /**
     * 更新资质
     */
    @PutMapping("/qualifications/{qualificationId}")
    public Result<QualificationVO> updateQualification(
            @PathVariable String qualificationId,
            @RequestBody QualificationUpdateDTO updateDTO) {
        QualificationVO vo = staffService.updateQualification(qualificationId, updateDTO);
        return Result.success(vo);
    }

    /**
     * 删除资质
     */
    @DeleteMapping("/qualifications/{qualificationId}")
    public Result<Void> deleteQualification(@PathVariable String qualificationId) {
        staffService.deleteQualification(qualificationId);
        return Result.success();
    }

    // ==================== 排班管理 ====================

    /**
     * 添加排班
     */
    @PostMapping("/{staffId}/schedules")
    public Result<ScheduleVO> addSchedule(
            @PathVariable String staffId,
            @RequestBody ScheduleCreateDTO createDTO) {
        ScheduleVO vo = staffService.addSchedule(staffId, createDTO);
        return Result.success(vo);
    }

    /**
     * 获取服务人员排班列表
     */
    @GetMapping("/{staffId}/schedules")
    public Result<List<ScheduleVO>> getSchedules(@PathVariable String staffId) {
        List<ScheduleVO> list = staffService.getSchedules(staffId);
        return Result.success(list);
    }

    /**
     * 更新排班
     */
    @PutMapping("/schedules/{scheduleId}")
    public Result<ScheduleVO> updateSchedule(
            @PathVariable String scheduleId,
            @RequestBody ScheduleUpdateDTO updateDTO) {
        ScheduleVO vo = staffService.updateSchedule(scheduleId, updateDTO);
        return Result.success(vo);
    }

    /**
     * 删除排班
     */
    @DeleteMapping("/schedules/{scheduleId}")
    public Result<Void> deleteSchedule(@PathVariable String scheduleId) {
        staffService.deleteSchedule(scheduleId);
        return Result.success();
    }

    /**
     * 查询某日期的排班
     */
    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> getSchedulesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleVO> list = staffService.getSchedulesByDate(date);
        return Result.success(list);
    }

    // ==================== 签到签退 ====================

    /**
     * 签到
     */
    @PostMapping("/check-in")
    public Result<WorkRecordVO> checkIn(@RequestBody CheckInDTO checkInDTO) {
        WorkRecordVO vo = staffService.checkIn(checkInDTO);
        return Result.success(vo);
    }

    /**
     * 签退
     */
    @PostMapping("/check-out")
    public Result<WorkRecordVO> checkOut(@RequestBody CheckOutDTO checkOutDTO) {
        WorkRecordVO vo = staffService.checkOut(checkOutDTO);
        return Result.success(vo);
    }

    /**
     * 查询签到记录
     */
    @GetMapping("/work-records")
    public Result<PageResult<WorkRecordVO>> queryWorkRecords(
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer checkInStatus,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        WorkRecordQueryDTO queryDTO = new WorkRecordQueryDTO();
        queryDTO.setStaffId(staffId);
        queryDTO.setOrderId(orderId);
        queryDTO.setCheckInStatus(checkInStatus);
        queryDTO.setStatus(status);
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        PageResult<WorkRecordVO> result = staffService.queryWorkRecords(queryDTO);
        return Result.success(result);
    }
}
