package com.elderlycare.dto.appointment;

import lombok.Data;

/**
 * 预约查询DTO
 */
@Data
public class AppointmentQueryDTO {

    /** 预约单号 */
    private String appointmentNo;

    /** 老人姓名 */
    private String elderName;

    /** 老人手机号 */
    private String elderPhone;

    /** 服务类型 */
    private String serviceType;

    /** 服务类型编码 */
    private String serviceTypeCode;

    /** 预约状态 */
    private String status;

    /** 区域ID */
    private String areaId;

    /** 服务商ID */
    private String providerId;

    /** 服务人员ID（STAFF角色数据隔离） */
    private String staffId;

    /** 开始日期 */
    private String startDate;

    /** 结束日期 */
    private String endDate;

    /** 当前页 */
    private Integer current = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
