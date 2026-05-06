package com.elderlycare.dto.appointment;

import lombok.Data;

/**
 * 预约编辑 DTO — 管理员补充业务字段
 */
@Data
public class AppointmentUpdateDTO {
    /** 服务类型名称，如 上门服务 */
    private String serviceType;
    /** 服务类型编码，如 DOOR_TO_DOOR */
    private String serviceTypeCode;
    /** 预约时间，如 2026-05-10 09:00 */
    private String appointmentTime;
    /** 备注 */
    private String remark;
}
