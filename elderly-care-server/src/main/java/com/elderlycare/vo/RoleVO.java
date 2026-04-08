package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色视图对象
 */
@Data
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private String roleId;

    /** 角色代码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 角色描述 */
    private String roleDesc;

    /** 角色类型 */
    private String roleType;

    /** 数据范围 */
    private String dataScope;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 */
    private String status;

    /** 租户ID */
    private String tenantId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 权限列表 */
    private List<PermissionVO> permissions;
}
