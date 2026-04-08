package com.elderlycare.dto.elder;

import lombok.Data;
import java.io.Serializable;

/**
 * 老人家庭信息DTO
 */
@Data
public class ElderFamilyDTO implements Serializable {

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
     * 与老人关系：0-配偶，1-子女，2-兄弟姐妹，3-孙子女，4-其他亲属，5-朋友，6-保姆，7-其他
     */
    private Integer relation;

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
     * 能否代为决策：0-否，1-是
     */
    private Integer canDecide;

    /**
     * 备注
     */
    private String remark;
}
