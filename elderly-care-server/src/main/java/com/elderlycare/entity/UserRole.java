package com.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Data
@TableName("t_user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户角色ID */
    @TableId(type = IdType.ASSIGN_ID)
    private String userRoleId;

    /** 用户ID */
    private String userId;

    /** 角色ID */
    private String roleId;

    /** 区域ID */
    private String areaId;

    /** 租户ID */
    private String tenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
