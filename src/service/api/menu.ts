import { request } from '../request';

/**
 * 获取菜单列表（树形）
 */
export function fetchGetMenuTree() {
  return request<Api.User.Menu[]>({
    url: '/system/menu/tree',
    method: 'get'
  });
}

/**
 * 获取菜单列表（平铺）
 */
export function fetchGetMenuList(params?: { menuType?: string; status?: string }) {
  return request<Api.User.Menu[]>({
    url: '/system/menu/list',
    method: 'get',
    params
  });
}

/**
 * 获取菜单详情
 */
export function fetchGetMenu(id: string) {
  return request<Api.User.Menu>({
    url: `/system/menu/${id}`,
    method: 'get'
  });
}

/**
 * 创建菜单
 */
export function fetchCreateMenu(data: Api.User.Menu & { id?: string }) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  });
}

/**
 * 更新菜单
 */
export function fetchUpdateMenu(id: string, data: Api.User.Menu) {
  return request({
    url: `/system/menu/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除菜单
 */
export function fetchDeleteMenu(id: string) {
  return request({
    url: `/system/menu/${id}`,
    method: 'delete'
  });
}

/**
 * 获取菜单权限（根据角色ID）
 */
export function fetchGetMenuPermissions(roleId: string) {
  return request<string[]>({
    url: `/system/menu/permissions/${roleId}`,
    method: 'get'
  });
}

/**
 * 分配菜单权限
 */
export function fetchAssignPermissions(roleId: string, menuIds: string[]) {
  return request({
    url: `/system/menu/assign`,
    method: 'put',
    data: { roleId, menuIds }
  });
}
