package com.elderlycare.vo.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务人员VO
 */
@Data
public class StaffVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String staffId;

    /**
     * 所属服务商ID
     */
    private String providerId;

    /**
     * 服务商名称
     */
    private String providerName;

    /**
     * 员工编号
     */
    private String staffNo;

    /**
     * 姓名
     */
    private String staffName;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 性别文本
     */
    private String genderText;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 民族
     */
    private String nation;

    /**
     * 学历
     */
    private String education;

    /**
     * 学历文本
     */
    private String educationText;

    /**
     * 政治面貌
     */
    private String politicalStatus;

    /**
     * 政治面貌文本
     */
    private String politicalStatusText;

    /**
     * 婚姻状况
     */
    private String maritalStatus;

    /**
     * 婚姻状况文本
     */
    private String maritalStatusText;

    /**
     * 户籍地址
     */
    private String domicileAddress;

    /**
     * 居住地址
     */
    private String residenceAddress;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 服务类型：多个用逗号分隔，如：1,2,3
     */
    private String serviceTypes;

    /**
     * 服务类型文本
     */
    private String serviceTypesText;

    /**
     * 员工状态：ON_JOB-在职，OFF_JOB-离职
     */
    private String status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 离职日期
     */
    private LocalDate leaveDate;

    /**
     * 离职原因
     */
    private String leaveReason;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
