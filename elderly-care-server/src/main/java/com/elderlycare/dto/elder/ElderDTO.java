package com.elderlycare.dto.elder;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 老人档案DTO
 */
@Data
public class ElderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 老人姓名
     */
    private String name;

    /**
     * 性别：0-男，1-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 所在区域-省
     */
    private String province;

    /**
     * 所在区域-市
     */
    private String city;

    /**
     * 所在区域-区/县
     */
    private String district;

    /**
     * 所在区域-街道/乡镇
     */
    private String street;

    /**
     * 所在区域-社区
     */
    private String community;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 户籍类型：0-本地户籍，1-外地户籍
     */
    private Integer householdType;

    /**
     * 婚姻状况：0-未婚，1-已婚，2-离异，3-丧偶
     */
    private Integer maritalStatus;

    /**
     * 居住情况：0-独居，1-与配偶同住，2-与子女同住，3-与保姆同住，4-养老机构
     */
    private Integer livingSituation;

    /**
     * 经济来源：0-退休金，1-子女供养，2-低保，3-其他
     */
    private Integer economicSource;

    /**
     * 月收入范围
     */
    private String monthlyIncome;

    /**
     * 是否有医保：0-无，1-有
     */
    private Integer hasMedicalInsurance;

    /**
     * 医保类型：0-城镇职工医保，1-城乡居民医保，2-商业医保
     */
    private Integer medicalInsuranceType;

    /**
     * 残疾等级：0-无残疾，1-一级，2-二级，3-三级，4-四级
     */
    private Integer disabilityLevel;

    /**
     * 是否有残疾证：0-无，1-有
     */
    private Integer hasDisabilityCertificate;

    /**
     * 护理等级：0-不需要护理，1-轻度失能，2-中度失能，3-重度失能
     */
    private Integer careLevel;

    /**
     * 紧急联系人姓名
     */
    private String emergencyContactName;

    /**
     * 紧急联系人电话
     */
    private String emergencyContactPhone;

    /**
     * 紧急联系人关系
     */
    private String emergencyContactRelation;
}
