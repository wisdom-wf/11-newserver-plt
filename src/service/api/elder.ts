import { request } from '../request';

/**
 * 获取老人列表
 */
export function fetchGetElderList(params?: Api.Elder.ElderQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Elder.Elder>>({
    url: '/api/elders',
    method: 'get',
    params
  });
}

/**
 * 获取老人详情
 */
export function fetchGetElder(id: string) {
  return request<Api.Elder.Elder>({
    url: `/api/elders/${id}`,
    method: 'get'
  });
}

/**
 * 创建老人
 */
export function fetchCreateElder(data: Api.Elder.ElderForm) {
  return request({
    url: '/api/elders',
    method: 'post',
    data
  });
}

/**
 * 更新老人
 */
export function fetchUpdateElder(id: string, data: Api.Elder.ElderForm) {
  return request({
    url: `/api/elders/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除老人
 */
export function fetchDeleteElder(id: string) {
  return request({
    url: `/api/elders/${id}`,
    method: 'delete'
  });
}

/**
 * 更新老人状态
 */
export function fetchUpdateElderStatus(id: string, status: Api.Common.EnableStatus) {
  return request({
    url: `/api/elders/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 获取老人统计
 */
export function fetchGetElderStatistics(params?: { areaId?: string; communityId?: string }) {
  return request<Api.Elder.Statistics>({
    url: '/api/statistics/elder',
    method: 'get',
    params
  });
}

/**
 * 获取老人选择列表
 */
export function fetchGetElderOptions(params?: { areaId?: string; communityId?: string }) {
  return request<{ id: string; name: string; phone?: string }[]>({
    url: '/api/elders/options',
    method: 'get',
    params
  });
}

/**
 * 通过身份证号获取老人信息
 */
export function fetchGetElderByIdCard(idCard: string) {
  return request<Api.Elder.Elder>({
    url: `/api/elders/idCard/${idCard}`,
    method: 'get'
  });
}
