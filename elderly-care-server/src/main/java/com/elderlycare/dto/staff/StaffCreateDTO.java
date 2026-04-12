package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 服务人员创建DTO
 */
@Data
public class StaffCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属服务商ID
     */
    private String providerId;

    /**
     * 姓名
     */
    private String staffName;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

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
     * 政治面貌
     */
    private String politicalStatus;

    /**
     * 婚姻状况
     */
    private String maritalStatus;

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
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 备注
     */
    private String remark;
}
