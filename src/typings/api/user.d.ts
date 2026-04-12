/**
 * 用户管理模块
 */

declare namespace Api {
  namespace User {
    /** 用户查询参数 */
    interface UserQuery {
      /** 用户名 */
      userName?: string;
      /** 手机号 */
      phone?: string;
      /** 用户类型 */
      userType?: string;
      /** 状态 */
      status?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
    }

    /** 用户表单 */
    interface UserForm {
      /** 用户ID（编辑时需要） */
      id?: string;
      /** 用户名 */
      userName: string;
      /** 密码（创建时需要） */
      password?: string;
      /** 真实姓名 */
      realName: string;
      /** 手机号 */
      phone?: string;
      /** 邮箱 */
      email?: string;
      /** 用户类型 */
      userType: string;
      /** 状态 */
      status: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 角色ID列表 */
      roleIds?: string[];
      /** 备注 */
      remark?: string;
    }

    /** 用户详情 */
    interface User {
      /** 用户ID */
      id: string;
      /** 用户名 */
      userName: string;
      /** 真实姓名 */
      realName: string;
      /** 手机号 */
      phone?: string;
      /** 邮箱 */
      email?: string;
      /** 头像 */
      avatar?: string;
      /** 用户类型 */
      userType: string;
      /** 用户类型名称 */
      userTypeName?: string;
      /** 状态 */
      status: string;
      /** 区域ID */
      areaId?: string;
      /** 区域名称 */
      areaName?: string;
      /** 服务商ID */
      providerId?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 角色列表 */
      roles: Role[];
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
      /** 最后登录时间 */
      lastLoginTime?: string;
    }

    /** 角色 */
    interface Role {
      /** 角色ID */
      id: string;
      /** 角色编码 */
      roleCode: string;
      /** 角色名称 */
      roleName: string;
      /** 角色类型 */
      roleType?: string;
      /** 数据范围 */
      dataScope?: string;
      /** 状态 */
      status: string;
      /** 排序 */
      sortOrder?: number;
      /** 创建时间 */
      createTime: string;
    }

    /** 角色查询参数 */
    interface RoleQuery {
      /** 角色名称 */
      roleName?: string;
      /** 角色类型 */
      roleType?: string;
      /** 状态 */
      status?: string;
    }

    /** 角色表单 */
    interface RoleForm {
      /** 角色ID（编辑时需要） */
      id?: string;
      /** 角色编码 */
      roleCode: string;
      /** 角色名称 */
      roleName: string;
      /** 角色类型 */
      roleType?: string;
      /** 数据范围 */
      dataScope?: string;
      /** 状态 */
      status: string;
      /** 排序 */
      sortOrder?: number;
      /** 备注 */
      remark?: string;
      /** 权限ID列表 */
      permissionIds?: string[];
    }

    /** 菜单权限 */
    interface Menu {
      /** 权限ID */
      permissionId: string;
      /** 权限编码 */
      permissionCode: string;
      /** 权限名称 */
      permissionName: string;
      /** 父权限ID */
      parentId?: string;
      /** 权限类型：MENU/BUTTON/API */
      permissionType: string;
      /** 权限URL */
      permissionUrl?: string;
      /** 请求方法 */
      permissionMethod?: string;
      /** 图标 */
      icon?: string;
      /** 排序 */
      sortOrder?: number;
      /** 状态 */
      status: string;
      /** 子菜单 */
      children?: Menu[];
      /** 别名id（兼容前端） */
      id?: string;
      /** 别名label（兼容前端） */
      label?: string;
    }

    /** 区域 */
    interface Area {
      /** 区域ID */
      id: string;
      /** 区域编码 */
      areaCode: string;
      /** 区域名称 */
      areaName: string;
      /** 父区域ID */
      parentId?: string;
      /** 区域层级：province/city/district/street/community */
      level: string;
      /** 经度 */
      longitude?: number;
      /** 纬度 */
      latitude?: number;
      /** 排序 */
      sortOrder?: number;
      /** 状态 */
      status: string;
      /** 子区域 */
      children?: Area[];
    }
  }
}
