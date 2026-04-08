package com.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体
 */
@Data
@TableName("t_role_permission")
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色权限ID */
    @TableId(type = IdType.ASSIGN_ID)
    private String rolePermissionId;

    /** 角色ID */
    private String roleId;

    /** 权限ID */
    private String permissionId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
