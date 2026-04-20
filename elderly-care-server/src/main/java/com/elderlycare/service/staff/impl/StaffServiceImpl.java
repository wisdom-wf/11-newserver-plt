package com.elderlycare.service.staff.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.DateUtil;
import com.elderlycare.dto.staff.*;
import com.elderlycare.entity.staff.Staff;
import com.elderlycare.entity.staff.StaffQualification;
import com.elderlycare.entity.staff.StaffSchedule;
import com.elderlycare.entity.staff.StaffWorkRecord;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.mapper.staff.StaffQualificationMapper;
import com.elderlycare.mapper.staff.StaffScheduleMapper;
import com.elderlycare.mapper.staff.StaffWorkRecordMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.service.staff.StaffService;
import com.elderlycare.vo.staff.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务人员Service实现
 */
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffMapper staffMapper;
    private final StaffQualificationMapper qualificationMapper;
    private final StaffScheduleMapper scheduleMapper;
    private final StaffWorkRecordMapper workRecordMapper;
    private final ServiceLogMapper serviceLogMapper;

    /**
     * 根据身份证号计算年龄（18位中国身份证）
     * 身份证号格式：6位地区码 + 8位出生日期(YYYYMMDD) + 3位顺序码 + 1位性别码 + 1位校验码
     */
    private int calculateAgeFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return 0;
        }
        try {
            int birthYear = Integer.parseInt(idCard.substring(6, 10));
            int birthMonth = Integer.parseInt(idCard.substring(10, 12));
            int birthDay = Integer.parseInt(idCard.substring(12, 14));
            LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
            LocalDate today = LocalDate.now();
            int age = today.getYear() - birthYear;
            if (today.getMonthValue() < birthMonth ||
                (today.getMonthValue() == birthMonth && today.getDayOfMonth() < birthDay)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== 服务人员管理 ====================

    @Override
    @Transactional
    public StaffVO createStaff(StaffCreateDTO createDTO) {
        // 生成员工编号
        String staffNo = "S" + DateUtil.generateNo("");

        Staff staff = new Staff();
        staff.setProviderId(createDTO.getProviderId());
        staff.setStaffNo(staffNo);
        staff.setStaffName(createDTO.getStaffName());
        staff.setGender(createDTO.getGender());
        staff.setIdCard(createDTO.getIdCard());
        staff.setPhone(createDTO.getPhone());
        staff.setBirthDate(createDTO.getBirthDate());
        staff.setNation(createDTO.getNation());
        staff.setEducation(createDTO.getEducation());
        staff.setPoliticalStatus(createDTO.getPoliticalStatus());
        staff.setMaritalStatus(createDTO.getMaritalStatus());
        staff.setDomicileAddress(createDTO.getDomicileAddress());
        staff.setResidenceAddress(createDTO.getResidenceAddress());
        staff.setEmergencyContact(createDTO.getEmergencyContact());
        staff.setEmergencyPhone(createDTO.getEmergencyPhone());
        staff.setServiceTypes(createDTO.getServiceTypes());
        staff.setStatus("PENDING"); // 待审核
        staff.setHireDate(createDTO.getHireDate());

        // Calculate age from ID card (18-digit Chinese ID card)
        if (createDTO.getIdCard() != null && createDTO.getIdCard().length() == 18) {
            int age = calculateAgeFromIdCard(createDTO.getIdCard());
            staff.setAge(age);
        }
        staff.setAvatarUrl(createDTO.getAvatarUrl());
        staff.setRemark(createDTO.getRemark());

        staffMapper.insert(staff);
        return convertToStaffVO(staff);
    }

    @Override
    public PageResult<StaffVO> queryStaff(StaffQueryDTO queryDTO) {
        Page<Staff> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<Staff> staffPage = staffMapper.selectStaffPage(page, queryDTO);

        List<StaffVO> voList = staffPage.getRecords().stream()
                .map(this::convertToStaffVO)
                .collect(Collectors.toList());

        return new PageResult<>(staffPage.getTotal(), queryDTO.getPage(), queryDTO.getPageSize(), voList);
    }

    @Override
    public StaffVO getStaffById(String staffId) {
        Staff staff = staffMapper.selectStaffById(staffId);
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }
        return convertToStaffVO(staff);
    }

    @Override
    @Transactional
    public StaffVO updateStaff(String staffId, StaffUpdateDTO updateDTO) {
        Staff staff = staffMapper.selectStaffById(staffId);
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }

        // 计算年龄
        Integer age = null;
        if (updateDTO.getIdCard() != null && updateDTO.getIdCard().length() == 18) {
            age = calculateAgeFromIdCard(updateDTO.getIdCard());
        }

        // 使用自定义SQL更新，绕过逻辑删除限制
        staffMapper.updateStaffById(
            staffId,
            updateDTO.getStaffName(),
            updateDTO.getGender(),
            updateDTO.getIdCard(),
            age,
            updateDTO.getPhone(),
            updateDTO.getBirthDate(),
            updateDTO.getNation(),
            updateDTO.getEducation(),
            updateDTO.getPoliticalStatus(),
            updateDTO.getMaritalStatus(),
            updateDTO.getDomicileAddress(),
            updateDTO.getResidenceAddress(),
            updateDTO.getEmergencyContact(),
            updateDTO.getEmergencyPhone(),
            updateDTO.getServiceTypes(),
            updateDTO.getHireDate(),
            updateDTO.getLeaveDate(),
            updateDTO.getLeaveReason(),
            updateDTO.getAvatarUrl(),
            updateDTO.getRemark(),
            updateDTO.getStatus()
        );

        return convertToStaffVO(staffMapper.selectStaffById(staffId));
    }

    @Override
    @Transactional
    public void deleteStaff(String staffId) {
        Staff staff = staffMapper.selectStaffById(staffId);
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }
        staffMapper.deleteById(staffId);
    }

    @Override
    @Transactional
    public void updateStaffStatus(String staffId, StaffStatusDTO statusDTO) {
        Staff staff = staffMapper.selectStaffById(staffId);
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }
        staff.setStatus(statusDTO.getStatus());
        staff.setAuditRemark(statusDTO.getAuditRemark());
        staffMapper.updateById(staff);
    }

    // ==================== 资质管理 ====================

    @Override
    @Transactional
    public QualificationVO addQualification(String staffId, QualificationCreateDTO createDTO) {
        Staff staff = staffMapper.selectStaffById(staffId);
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }

        StaffQualification qualification = new StaffQualification();
        qualification.setStaffId(staffId);
        qualification.setQualificationType(createDTO.getQualificationType());
        qualification.setQualificationName(createDTO.getQualificationName());
        qualification.setQualificationNo(createDTO.getQualificationNo());
        qualification.setIssuingAuthority(createDTO.getIssuingAuthority());
        qualification.setIssueDate(createDTO.getIssueDate());
        qualification.setExpireDate(createDTO.getExpireDate());
        qualification.setCertificateUrls(createDTO.getCertificateUrls());
        qualification.setStatus(0); // 有效
        qualification.setRemark(createDTO.getRemark());

        qualificationMapper.insert(qualification);
        return convertToQualificationVO(qualification);
    }

    @Override
    public List<QualificationVO> getQualifications(String staffId) {
        List<StaffQualification> qualifications = qualificationMapper.selectByStaffId(staffId);
        return qualifications.stream()
                .map(this::convertToQualificationVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QualificationVO updateQualification(String qualificationId, QualificationUpdateDTO updateDTO) {
        StaffQualification qualification = qualificationMapper.selectByQualificationId(qualificationId);
        if (qualification == null) {
            throw new BusinessException("资质不存在");
        }

        if (updateDTO.getQualificationType() != null) {
            qualification.setQualificationType(updateDTO.getQualificationType());
        }
        if (updateDTO.getQualificationName() != null) {
            qualification.setQualificationName(updateDTO.getQualificationName());
        }
        if (updateDTO.getQualificationNo() != null) {
            qualification.setQualificationNo(updateDTO.getQualificationNo());
        }
        if (updateDTO.getIssuingAuthority() != null) {
            qualification.setIssuingAuthority(updateDTO.getIssuingAuthority());
        }
        if (updateDTO.getIssueDate() != null) {
            qualification.setIssueDate(updateDTO.getIssueDate());
        }
        if (updateDTO.getExpireDate() != null) {
            qualification.setExpireDate(updateDTO.getExpireDate());
        }
        if (updateDTO.getCertificateUrls() != null) {
            qualification.setCertificateUrls(updateDTO.getCertificateUrls());
        }
        if (updateDTO.getRemark() != null) {
            qualification.setRemark(updateDTO.getRemark());
        }

        qualificationMapper.updateById(qualification);
        return convertToQualificationVO(qualification);
    }

    @Override
    @Transactional
    public void deleteQualification(String qualificationId) {
        StaffQualification qualification = qualificationMapper.selectByQualificationId(qualificationId);
        if (qualification == null) {
            throw new BusinessException("资质不存在");
        }
        qualificationMapper.deleteById(qualificationId);
    }

    // ==================== 排班管理 ====================

    @Override
    @Transactional
    public ScheduleVO addSchedule(String staffId, ScheduleCreateDTO createDTO) {
        Staff staff = staffMapper.selectStaffById(staffId);
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }

        StaffSchedule schedule = new StaffSchedule();
        schedule.setStaffId(staffId);
        schedule.setScheduleDate(createDTO.getScheduleDate());
        schedule.setShiftType(createDTO.getShiftType());
        schedule.setStartTime(createDTO.getStartTime());
        schedule.setEndTime(createDTO.getEndTime());
        schedule.setScheduleType(createDTO.getScheduleType());
        schedule.setOrderId(createDTO.getOrderId());
        schedule.setStatus(0); // 已排班
        schedule.setRemark(createDTO.getRemark());

        scheduleMapper.insert(schedule);
        return convertToScheduleVO(schedule);
    }

    @Override
    public List<ScheduleVO> getSchedules(String staffId) {
        List<StaffSchedule> schedules = scheduleMapper.selectByStaffId(staffId);
        return schedules.stream()
                .map(this::convertToScheduleVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleVO updateSchedule(String scheduleId, ScheduleUpdateDTO updateDTO) {
        StaffSchedule schedule = scheduleMapper.selectByScheduleId(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }

        if (updateDTO.getScheduleDate() != null) {
            schedule.setScheduleDate(updateDTO.getScheduleDate());
        }
        if (updateDTO.getShiftType() != null) {
            schedule.setShiftType(updateDTO.getShiftType());
        }
        if (updateDTO.getStartTime() != null) {
            schedule.setStartTime(updateDTO.getStartTime());
        }
        if (updateDTO.getEndTime() != null) {
            schedule.setEndTime(updateDTO.getEndTime());
        }
        if (updateDTO.getScheduleType() != null) {
            schedule.setScheduleType(updateDTO.getScheduleType());
        }
        if (updateDTO.getOrderId() != null) {
            schedule.setOrderId(updateDTO.getOrderId());
        }
        if (updateDTO.getStatus() != null) {
            schedule.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getRemark() != null) {
            schedule.setRemark(updateDTO.getRemark());
        }

        scheduleMapper.updateById(schedule);
        return convertToScheduleVO(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(String scheduleId) {
        StaffSchedule schedule = scheduleMapper.selectByScheduleId(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        scheduleMapper.deleteById(scheduleId);
    }

    @Override
    public List<ScheduleVO> getSchedulesByDate(LocalDate date) {
        List<StaffSchedule> schedules = scheduleMapper.selectByDate(date);
        return schedules.stream()
                .map(this::convertToScheduleVO)
                .collect(Collectors.toList());
    }

    // ==================== 签到签退 ====================

    @Override
    @Transactional
    public WorkRecordVO checkIn(CheckInDTO checkInDTO) {
        // 检查是否已签到
        StaffWorkRecord existingRecord = workRecordMapper.selectTodayRecordByStaffId(checkInDTO.getStaffId());
        if (existingRecord != null && existingRecord.getCheckInTime() != null) {
            throw new BusinessException("今日已签到，请勿重复签到");
        }

        Staff staff = staffMapper.selectStaffById(checkInDTO.getStaffId());
        if (staff == null) {
            throw new BusinessException("服务人员不存在");
        }

        StaffWorkRecord record = new StaffWorkRecord();
        record.setStaffId(checkInDTO.getStaffId());
        record.setScheduleId(checkInDTO.getScheduleId());
        record.setOrderId(checkInDTO.getOrderId());
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInLatitude(checkInDTO.getLatitude());
        record.setCheckInLongitude(checkInDTO.getLongitude());
        record.setCheckInAddress(checkInDTO.getAddress());
        record.setCheckInDevice(checkInDTO.getDevice());
        record.setStatus(0); // 正常

        // 检查是否迟到（如果有关联排班）
        if (checkInDTO.getScheduleId() != null) {
            StaffSchedule schedule = scheduleMapper.selectByScheduleId(checkInDTO.getScheduleId());
            if (schedule != null && schedule.getStartTime() != null) {
                LocalDateTime scheduledStart = schedule.getScheduleDate().atTime(schedule.getStartTime());
                if (LocalDateTime.now().isAfter(scheduledStart)) {
                    record.setCheckInStatus(1); // 迟到
                } else {
                    record.setCheckInStatus(0); // 正常
                }
            } else {
                record.setCheckInStatus(0);
            }
        } else {
            record.setCheckInStatus(0);
        }

        workRecordMapper.insert(record);
        return convertToWorkRecordVO(record);
    }

    @Override
    @Transactional
    public WorkRecordVO checkOut(CheckOutDTO checkOutDTO) {
        StaffWorkRecord record = workRecordMapper.selectByRecordId(checkOutDTO.getRecordId());
        if (record == null) {
            throw new BusinessException("工作记录不存在");
        }
        if (record.getCheckOutTime() != null) {
            throw new BusinessException("今日已签退，请勿重复签退");
        }

        record.setCheckOutTime(LocalDateTime.now());
        record.setCheckOutLatitude(checkOutDTO.getLatitude());
        record.setCheckOutLongitude(checkOutDTO.getLongitude());
        record.setCheckOutAddress(checkOutDTO.getAddress());
        record.setCheckOutDevice(checkOutDTO.getDevice());

        // 计算工作时长
        if (record.getCheckInTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(record.getCheckInTime(), record.getCheckOutTime());
            record.setWorkDuration((int) minutes);
        }

        // 检查是否早退
        if (checkOutDTO.getRecordId() != null) {
            StaffSchedule schedule = scheduleMapper.selectByScheduleId(record.getScheduleId());
            if (schedule != null && schedule.getEndTime() != null) {
                LocalDateTime scheduledEnd = schedule.getScheduleDate().atTime(schedule.getEndTime());
                if (record.getCheckOutTime().isBefore(scheduledEnd)) {
                    record.setCheckOutStatus(2); // 早退
                } else {
                    record.setCheckOutStatus(0); // 正常
                }
            } else {
                record.setCheckOutStatus(0);
            }
        } else {
            record.setCheckOutStatus(0);
        }

        workRecordMapper.updateById(record);
        return convertToWorkRecordVO(record);
    }

    @Override
    public PageResult<WorkRecordVO> queryWorkRecords(WorkRecordQueryDTO queryDTO) {
        Page<StaffWorkRecord> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<StaffWorkRecord> recordPage = workRecordMapper.selectWorkRecordPage(page, queryDTO);

        List<WorkRecordVO> voList = recordPage.getRecords().stream()
                .map(this::convertToWorkRecordVO)
                .collect(Collectors.toList());

        return new PageResult<>(recordPage.getTotal(), queryDTO.getPage(), queryDTO.getPageSize(), voList);
    }

    // ==================== 转换方法 ====================

    private StaffVO convertToStaffVO(Staff staff) {
        if (staff == null) {
            return null;
        }
        StaffVO vo = new StaffVO();
        vo.setStaffId(staff.getStaffId());
        vo.setProviderId(staff.getProviderId());
        vo.setProviderName(staff.getProviderName());
        vo.setStaffNo(staff.getStaffNo());
        vo.setStaffName(staff.getStaffName());
        vo.setGender(staff.getGender());
        vo.setGenderText(getGenderText(staff.getGender()));
        vo.setIdCard(staff.getIdCard());
        vo.setPhone(staff.getPhone());
        vo.setAge(staff.getAge());
        vo.setBirthDate(staff.getBirthDate());
        vo.setNation(staff.getNation());
        vo.setEducation(staff.getEducation());
        vo.setEducationText(getEducationText(staff.getEducation()));
        vo.setPoliticalStatus(staff.getPoliticalStatus());
        vo.setPoliticalStatusText(getPoliticalStatusText(staff.getPoliticalStatus()));
        vo.setMaritalStatus(staff.getMaritalStatus());
        vo.setMaritalStatusText(getMaritalStatusText(staff.getMaritalStatus()));
        vo.setDomicileAddress(staff.getDomicileAddress());
        vo.setResidenceAddress(staff.getResidenceAddress());
        vo.setEmergencyContact(staff.getEmergencyContact());
        vo.setEmergencyPhone(staff.getEmergencyPhone());
        vo.setServiceTypes(staff.getServiceTypes());
        vo.setServiceTypesText(staff.getServiceTypes()); // 后续可转换
        vo.setStatus(staff.getStatus());
        vo.setStatusText(getStatusText(staff.getStatus()));
        vo.setAuditRemark(staff.getAuditRemark());
        vo.setHireDate(staff.getHireDate());
        vo.setLeaveDate(staff.getLeaveDate());
        vo.setLeaveReason(staff.getLeaveReason());
        vo.setAvatarUrl(staff.getAvatarUrl());
        vo.setRemark(staff.getRemark());
        vo.setCreateTime(staff.getCreateTime());
        vo.setUpdateTime(staff.getUpdateTime());
        return vo;
    }

    private QualificationVO convertToQualificationVO(StaffQualification qualification) {
        if (qualification == null) {
            return null;
        }
        QualificationVO vo = new QualificationVO();
        vo.setQualificationId(qualification.getQualificationId());
        vo.setStaffId(qualification.getStaffId());
        vo.setQualificationType(qualification.getQualificationType());
        vo.setQualificationTypeText(getQualificationTypeText(qualification.getQualificationType()));
        vo.setQualificationName(qualification.getQualificationName());
        vo.setQualificationNo(qualification.getQualificationNo());
        vo.setIssuingAuthority(qualification.getIssuingAuthority());
        vo.setIssueDate(qualification.getIssueDate());
        vo.setExpireDate(qualification.getExpireDate());
        vo.setCertificateUrls(qualification.getCertificateUrls());
        vo.setStatus(qualification.getStatus());
        vo.setStatusText(getQualificationStatusText(qualification.getStatus()));
        vo.setRemark(qualification.getRemark());
        vo.setCreateTime(qualification.getCreateTime());
        vo.setUpdateTime(qualification.getUpdateTime());
        return vo;
    }

    private ScheduleVO convertToScheduleVO(StaffSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        ScheduleVO vo = new ScheduleVO();
        vo.setScheduleId(schedule.getScheduleId());
        vo.setStaffId(schedule.getStaffId());
        vo.setScheduleDate(schedule.getScheduleDate());
        vo.setShiftType(schedule.getShiftType());
        vo.setShiftTypeText(getShiftTypeText(schedule.getShiftType()));
        vo.setStartTime(schedule.getStartTime());
        vo.setEndTime(schedule.getEndTime());
        vo.setScheduleType(schedule.getScheduleType());
        vo.setScheduleTypeText(getScheduleTypeText(schedule.getScheduleType()));
        vo.setOrderId(schedule.getOrderId());
        vo.setStatus(schedule.getStatus());
        vo.setStatusText(getScheduleStatusText(schedule.getStatus()));
        vo.setRemark(schedule.getRemark());
        vo.setCreateTime(schedule.getCreateTime());
        vo.setUpdateTime(schedule.getUpdateTime());
        return vo;
    }

    private WorkRecordVO convertToWorkRecordVO(StaffWorkRecord record) {
        if (record == null) {
            return null;
        }
        WorkRecordVO vo = new WorkRecordVO();
        vo.setRecordId(record.getRecordId());
        vo.setStaffId(record.getStaffId());
        vo.setScheduleId(record.getScheduleId());
        vo.setOrderId(record.getOrderId());
        vo.setCheckInTime(record.getCheckInTime());
        vo.setCheckInLatitude(record.getCheckInLatitude());
        vo.setCheckInLongitude(record.getCheckInLongitude());
        vo.setCheckInAddress(record.getCheckInAddress());
        vo.setCheckInDevice(record.getCheckInDevice());
        vo.setCheckOutTime(record.getCheckOutTime());
        vo.setCheckOutLatitude(record.getCheckOutLatitude());
        vo.setCheckOutLongitude(record.getCheckOutLongitude());
        vo.setCheckOutAddress(record.getCheckOutAddress());
        vo.setCheckOutDevice(record.getCheckOutDevice());
        vo.setWorkDuration(record.getWorkDuration());
        vo.setCheckInStatus(record.getCheckInStatus());
        vo.setCheckInStatusText(getCheckInStatusText(record.getCheckInStatus()));
        vo.setCheckOutStatus(record.getCheckOutStatus());
        vo.setCheckOutStatusText(getCheckOutStatusText(record.getCheckOutStatus()));
        vo.setStatus(record.getStatus());
        vo.setStatusText(getWorkRecordStatusText(record.getStatus()));
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        return vo;
    }

    // ==================== 枚举文本转换 ====================

    private String getGenderText(Integer gender) {
        if (gender == null) return "";
        return switch (gender) {
            case 0 -> "女";
            case 1 -> "男";
            default -> "未知";
        };
    }

    private String getEducationText(String education) {
        if (education == null) return "";
        return switch (education) {
            case "PRIMARY" -> "小学";
            case "JUNIOR" -> "初中";
            case "SENIOR" -> "高中";
            case "SECONDARY" -> "中专";
            case "COLLEGE" -> "大专";
            case "BACHELOR" -> "本科";
            case "MASTER" -> "硕士";
            case "DOCTOR" -> "博士";
            default -> education;
        };
    }

    private String getPoliticalStatusText(String politicalStatus) {
        if (politicalStatus == null) return "";
        return switch (politicalStatus) {
            case "PUBLIC" -> "群众";
            case "MEMBER" -> "共青团员";
            case "COMMUNIST" -> "中共党员";
            case "OTHER" -> "其他";
            default -> politicalStatus;
        };
    }

    private String getMaritalStatusText(String maritalStatus) {
        if (maritalStatus == null) return "";
        return switch (maritalStatus) {
            case "UNMARRIED" -> "未婚";
            case "MARRIED" -> "已婚";
            case "DIVORCED" -> "离异";
            case "WIDOWED" -> "丧偶";
            default -> maritalStatus;
        };
    }

    private String getStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "待审核";
            case "ON_JOB" -> "正常";
            case "OFF_JOB" -> "离职";
            default -> "未知";
        };
    }

    private String getQualificationTypeText(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 0 -> "身份证";
            case 1 -> "健康证";
            case 2 -> "职业资格证";
            case 3 -> "培训证书";
            case 4 -> "无犯罪记录证明";
            case 5 -> "其他";
            default -> "未知";
        };
    }

    private String getQualificationStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "有效";
            case 1 -> "即将到期";
            case 2 -> "已过期";
            default -> "未知";
        };
    }

    private String getShiftTypeText(Integer shiftType) {
        if (shiftType == null) return "";
        return switch (shiftType) {
            case 0 -> "早班";
            case 1 -> "中班";
            case 2 -> "晚班";
            case 3 -> "全天";
            case 4 -> "休息";
            default -> "未知";
        };
    }

    private String getScheduleTypeText(Integer scheduleType) {
        if (scheduleType == null) return "";
        return switch (scheduleType) {
            case 0 -> "日常工作";
            case 1 -> "加班";
            case 2 -> "调休";
            case 3 -> "请假";
            default -> "未知";
        };
    }

    private String getScheduleStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "已排班";
            case 1 -> "已确认";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    private String getCheckInStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "迟到";
            case 2 -> "早退";
            case 3 -> "旷工";
            default -> "未知";
        };
    }

    private String getCheckOutStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "迟到";
            case 2 -> "早退";
            default -> "未知";
        };
    }

    private String getWorkRecordStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "异常";
            default -> "未知";
        };
    }

    @Override
    public List<com.elderlycare.vo.servicelog.ServiceLogVO> getServiceLogs(String staffId, int limit) {
        List<ServiceLog> logs = serviceLogMapper.selectServiceLogsByStaffId(staffId, limit);
        return logs.stream().map(this::convertToServiceLogVO).collect(Collectors.toList());
    }

    private com.elderlycare.vo.servicelog.ServiceLogVO convertToServiceLogVO(ServiceLog log) {
        com.elderlycare.vo.servicelog.ServiceLogVO vo = new com.elderlycare.vo.servicelog.ServiceLogVO();
        vo.setServiceLogId(log.getServiceLogId());
        vo.setOrderId(log.getOrderId());
        vo.setOrderNo(log.getOrderNo());
        vo.setElderId(log.getElderId());
        vo.setElderName(log.getElderName());
        vo.setStaffId(log.getStaffId());
        vo.setStaffName(log.getStaffName());
        vo.setServiceType(log.getServiceTypeCode());
        vo.setServiceCategory(log.getServiceTypeName());
        vo.setServiceDate(log.getServiceDate());
        vo.setServiceStartTime(log.getServiceStartTime() != null ? log.getServiceStartTime().toString() : null);
        vo.setServiceEndTime(log.getServiceEndTime() != null ? log.getServiceEndTime().toString() : null);
        vo.setServiceDuration(log.getServiceDuration());
        vo.setActualDuration(log.getActualDuration());
        vo.setServiceComment(log.getServiceComment());
        vo.setCreateTime(log.getCreateTime() != null ? log.getCreateTime().toString() : null);
        vo.setHealthObservations(log.getHealthObservations());
        vo.setMedicationGiven(log.getMedicationGiven());
        vo.setAnomalyType(log.getAnomalyType());
        vo.setAnomalyDesc(log.getAnomalyDesc());
        vo.setAuditStatus(log.getAuditStatus());
        return vo;
    }
}
