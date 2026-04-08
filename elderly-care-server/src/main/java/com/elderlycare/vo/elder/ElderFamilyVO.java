package com.elderlycare.vo.elder;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 老人家庭信息VO
 */
@Data
public class ElderFamilyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家庭信息ID
     */
    private String familyId;

    /**
     * 老人ID
     */
    private String elderId;

    /**
     * 家庭成员姓名
     */
    private String name;

    /**
     * 性别：0-男，1-女
     */
    private Integer gender;

    /**
     * 性别名称
     */
    private String genderName;

    /**
     * 与老人关系：0-配偶，1-子女，2-兄弟姐妹，3-孙子女，4-其他亲属，5-朋友，6-保姆，7-其他
     */
    private Integer relation;

    /**
     * 关系名称
     */
    private String relationName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 工作单位
     */
    private String workplace;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 是否主要联系人：0-否，1-是
     */
    private Integer isPrimary;

    /**
     * 是否主要联系人名称
     */
    private String isPrimaryName;

    /**
     * 能否代为决策：0-否，1-是
     */
    private Integer canDecide;

    /**
     * 能否代为决策名称
     */
    private String canDecideName;

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
