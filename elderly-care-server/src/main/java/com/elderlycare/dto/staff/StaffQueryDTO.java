package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 服务人员查询DTO
 */
@Data
public class StaffQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 服务人员编号
     */
    private String staffNo;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 服务人员状态：0-待审核，1-正常，2-禁用，3-离职
     */
    private Integer status;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 入职日期开始
     */
    private LocalDate hireDateStart;

    /**
     * 入职日期结束
     */
    private LocalDate hireDateEnd;
}
