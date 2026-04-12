import { request } from '../request';

/**
 * 获取角色列表
 */
export function fetchGetRoleList(params?: Api.User.RoleQuery) {
  return request<Api.Common.PaginatingQueryRecord<Api.User.Role>>({
    url: '/api/system/roles',
    method: 'get',
    params
  });
}

/**
 * 获取角色详情
 */
export function fetchGetRole(id: string) {
  return request<Api.User.Role>({
    url: `/api/system/roles/${id}`,
    method: 'get'
  });
}

/**
 * 创建角色
 */
export function fetchCreateRole(data: Api.User.RoleForm) {
  return request({
    url: '/api/system/roles',
    method: 'post',
    data
  });
}

/**
 * 更新角色
 */
export function fetchUpdateRole(id: string, data: Api.User.RoleForm) {
  return request({
    url: `/api/system/roles/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除角色
 */
export function fetchDeleteRole(id: string) {
  return request({
    url: `/api/system/roles/${id}`,
    method: 'delete'
  });
}

/**
 * 修改角色状态
 */
export function fetchUpdateRoleStatus(id: string, status: string) {
  return request({
    url: `/api/system/roles/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 获取角色菜单权限
 */
export function fetchGetRoleMenus(roleId: string) {
  return request<string[]>({
    url: `/api/system/roles/${roleId}/permissions`,
    method: 'get'
  });
}

/**
 * 分配角色菜单权限
 */
export function fetchAssignRoleMenus(roleId: string, menuIds: string[]) {
  return request({
    url: `/api/system/roles/${roleId}/permissions`,
    method: 'put',
    data: { menuIds }
  });
}

/**
 * 获取所有角色（下拉框用）
 */
export function fetchGetAllRoles() {
  return request<Api.User.Role[]>({
    url: '/api/system/roles/all',
    method: 'get'
  });
}
