import { request } from '../request';

/**
 * 获取菜单列表（树形）
 */
export function fetchGetMenuTree() {
  return request<Api.User.Menu[]>({
    url: '/api/system/permissions/tree',
    method: 'get'
  });
}

/**
 * 获取菜单列表（平铺）
 */
export function fetchGetMenuList(params?: { menuType?: string; status?: string }) {
  return request<Api.User.Menu[]>({
    url: '/api/system/permissions/tree',
    method: 'get',
    params
  });
}

/**
 * 获取菜单详情
 */
export function fetchGetMenu(id: string) {
  return request<Api.User.Menu>({
    url: `/api/system/permissions/${id}`,
    method: 'get'
  });
}

/**
 * 创建菜单
 */
export function fetchCreateMenu(data: Api.User.Menu & { id?: string }) {
  return request({
    url: '/api/system/permissions',
    method: 'post',
    data
  });
}

/**
 * 更新菜单
 */
export function fetchUpdateMenu(id: string, data: Api.User.Menu) {
  return request({
    url: `/api/system/permissions/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除菜单
 */
export function fetchDeleteMenu(id: string) {
  return request({
    url: `/api/system/permissions/${id}`,
    method: 'delete'
  });
}

/**
 * 获取菜单权限（根据角色ID）
 */
export function fetchGetMenuPermissions(roleId: string) {
  return request<string[]>({
    url: `/api/system/permissions/role/${roleId}`,
    method: 'get'
  });
}

/**
 * 分配菜单权限
 */
export function fetchAssignPermissions(roleId: string, menuIds: string[]) {
  return request({
    url: `/api/system/roles/${roleId}/permissions`,
    method: 'put',
    data: { menuIds }
  });
}

/**
 * 获取当前用户的菜单（动态菜单API）
 */
export function fetchGetUserMenus() {
  return request<Api.Menu.MenuTree[]>({
    url: '/api/menu/user',
    method: 'get'
  });
}
