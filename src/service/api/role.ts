import { request } from '../request';

/**
 * 获取角色列表
 */
export function fetchGetRoleList(params?: Api.User.RoleQuery) {
  return request<Api.Common.PaginatingQueryRecord<Api.User.Role>>({
    url: '/system/role/list',
    method: 'get',
    params
  });
}

/**
 * 获取角色详情
 */
export function fetchGetRole(id: string) {
  return request<Api.User.Role>({
    url: `/system/role/${id}`,
    method: 'get'
  });
}

/**
 * 创建角色
 */
export function fetchCreateRole(data: Api.User.RoleForm) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  });
}

/**
 * 更新角色
 */
export function fetchUpdateRole(id: string, data: Api.User.RoleForm) {
  return request({
    url: `/system/role/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除角色
 */
export function fetchDeleteRole(id: string) {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  });
}

/**
 * 修改角色状态
 */
export function fetchUpdateRoleStatus(id: string, status: string) {
  return request({
    url: `/system/role/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 获取角色菜单权限
 */
export function fetchGetRoleMenus(roleId: string) {
  return request<string[]>({
    url: `/system/role/${roleId}/menus`,
    method: 'get'
  });
}

/**
 * 分配角色菜单权限
 */
export function fetchAssignRoleMenus(roleId: string, menuIds: string[]) {
  return request({
    url: `/system/role/${roleId}/menus`,
    method: 'put',
    data: { menuIds }
  });
}

/**
 * 获取所有角色（下拉框用）
 */
export function fetchGetAllRoles() {
  return request<Api.User.Role[]>({
    url: '/system/role/all',
    method: 'get'
  });
}
