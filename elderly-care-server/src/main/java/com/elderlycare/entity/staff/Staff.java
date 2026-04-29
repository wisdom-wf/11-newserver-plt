package com.elderlycare.entity.staff;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务人员实体
 */
@Data
@TableName("t_staff")
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String staffId;

    /**
     * 所属服务商ID
     */
    private String providerId;

    /**
     * 所属服务商名称（非数据库字段）
     */
    @TableField(exist = false)
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
     * 性别：FEMALE-女，MALE-男
     */
    private String gender;

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
     * 员工状态：ON_JOB-在职，OFF_JOB-离职
     */
    private String status;

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
     * 年龄
     */
    private Integer age;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 工作状态：IDLE-空闲，ON_JOB-工作中，OFF_DUTY-下班
     */
    private String workStatus;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /**
     * 创建者ID
     */
    @TableField(exist = false)
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    @TableField(exist = false)
    private Long updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
