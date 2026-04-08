package com.elderlycare.entity.elder;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 老人家庭信息实体
 */
@Data
@TableName("t_elder_family")
public class ElderFamily implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家庭信息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private String updaterId;

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
