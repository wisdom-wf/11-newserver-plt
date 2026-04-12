import { request } from '../request';

/**
 * 获取区域树
 */
export function fetchGetAreaTree() {
  return request<Api.User.Area[]>({
    url: '/api/config/areas/tree',
    method: 'get'
  });
}

/**
 * 获取区域列表
 */
export function fetchGetAreaList(params?: { parentId?: string; level?: string }) {
  return request<Api.User.Area[]>({
    url: '/api/config/areas',
    method: 'get',
    params
  });
}

/**
 * 获取区域详情
 */
export function fetchGetArea(id: string) {
  return request<Api.User.Area>({
    url: `/api/config/areas/${id}`,
    method: 'get'
  });
}

/**
 * 创建区域
 */
export function fetchCreateArea(data: Api.User.Area & { id?: string }) {
  return request({
    url: '/api/config/areas',
    method: 'post',
    data
  });
}

/**
 * 更新区域
 */
export function fetchUpdateArea(id: string, data: Api.User.Area) {
  return request({
    url: `/api/config/areas/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除区域
 */
export function fetchDeleteArea(id: string) {
  return request({
    url: `/api/config/areas/${id}`,
    method: 'delete'
  });
}
