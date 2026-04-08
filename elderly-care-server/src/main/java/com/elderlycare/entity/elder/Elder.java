package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老人档案实体
 */
@Data
@TableName("t_elder")
public class Elder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 老人ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String elderId;

    /**
     * 老人姓名
     */
    private String name;

    /**
     * 性别：MALE/FEMALE
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 民族
     */
    private String ethnicity;

    /**
     * 教育程度
     */
    private String education;

    /**
     * 婚姻状况
     */
    private String maritalStatus;

    /**
     * 政治面貌
     */
    private String politicalStatus;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 状态：ACTIVE/PENDING/SUSPENDED/CANCELLED
     */
    private String status;

    /**
     * 登记日期
     */
    private LocalDate registerDate;

    /**
     * 居住地址
     */
    private String address;

    /**
     * 护理等级：NORMAL/SUBSIDIZED/FULL
     */
    private String careLevel;

    /**
     * 居住状态：ALONE/WITH_SPOUSE/WITH_CHILDREN/IN_NURSING_HOME/OTHER
     */
    private String livingStatus;

    /**
     * 健康状态：GOOD/FAIR/POOR
     */
    private String healthStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
