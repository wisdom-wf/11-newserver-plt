package com.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 */
@Data
@TableName("t_role_menu")
public class RoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String roleMenuId;

    /** 角色ID */
    private String roleId;

    /** 菜单ID */
    private String menuId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
