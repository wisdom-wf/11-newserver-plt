package com.elderlycare.dto.quality;

import lombok.Data;

/**
 * 质检查询DTO
 */
@Data
public class QualityCheckQueryDTO {

    /** 订单号 */
    private String orderNo;

    /** 服务商名称 */
    private String providerName;

    /** 服务人员姓名 */
    private String staffName;

    /** 服务类别 */
    private String serviceCategory;

    /** 质检类型 */
    private String checkType;

    /** 质检结果 */
    private String checkResult;

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
