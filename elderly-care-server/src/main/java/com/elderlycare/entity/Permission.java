package com.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("t_permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 权限ID */
    @TableId(type = IdType.ASSIGN_ID)
    private String permissionId;

    /** 权限代码 */
    private String permissionCode;

    /** 权限名称 */
    private String permissionName;

    /** 权限类型 */
    private String permissionType;

    /** 父权限ID */
    private String parentId;

    /** 权限URL */
    private String permissionUrl;

    /** 请求方法 */
    private String permissionMethod;

    /** 排序 */
    private Integer sortOrder;

    /** 图标 */
    private String icon;

    /** 状态 */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标记 */
    @TableLogic
    private Integer deleted;
}
