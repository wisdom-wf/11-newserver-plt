package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限视图对象
 */
@Data
public class PermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 权限ID */
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
    private LocalDateTime createTime;

    /** 子权限列表 */
    private List<PermissionVO> children;
}
