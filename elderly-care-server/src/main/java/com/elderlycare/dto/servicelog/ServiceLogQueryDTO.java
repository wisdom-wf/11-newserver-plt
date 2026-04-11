package com.elderlycare.dto.servicelog;

import lombok.Data;

/**
 * 服务日志查询DTO
 */
@Data
public class ServiceLogQueryDTO {

    /** 订单号 */
    private String orderNo;

    /** 老人姓名 */
    private String elderName;

    /** 服务人员姓名 */
    private String staffName;

    /** 服务类型 */
    private String serviceType;

    /** 服务状态 */
    private String serviceStatus;

    /** 区域ID */
    private String areaId;

    /** 服务商ID */
    private String providerId;

    /** 服务人员ID */
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
