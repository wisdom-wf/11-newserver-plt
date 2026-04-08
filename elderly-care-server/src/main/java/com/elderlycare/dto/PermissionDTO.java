package com.elderlycare.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 权限DTO
 */
@Data
public class PermissionDTO {

    /** 权限ID(修改时必填) */
    private String permissionId;

    @NotBlank(message = "权限代码不能为空")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
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
}
