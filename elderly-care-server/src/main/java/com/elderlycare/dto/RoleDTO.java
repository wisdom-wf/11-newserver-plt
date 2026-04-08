package com.elderlycare.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色DTO
 */
@Data
public class RoleDTO {

    /** 角色ID(修改时必填) */
    private String roleId;

    @NotBlank(message = "角色代码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
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

    /** 权限ID列表 */
    private List<String> permissionIds;
}
