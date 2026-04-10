import { request } from '../request';

/**
 * 获取服务商列表
 */
export function fetchGetProviderList(params?: Api.Provider.ProviderQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Provider.Provider>>({
    url: '/provider/list',
    method: 'get',
    params
  });
}

/**
 * 获取服务商详情
 */
export function fetchGetProvider(id: string) {
  return request<Api.Provider.Provider>({
    url: `/provider/${id}`,
    method: 'get'
  });
}

/**
 * 创建服务商
 */
export function fetchCreateProvider(data: Api.Provider.ProviderForm) {
  return request({
    url: '/provider',
    method: 'post',
    data
  });
}

/**
 * 更新服务商
 */
export function fetchUpdateProvider(id: string, data: Api.Provider.ProviderForm) {
  return request({
    url: `/provider/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除服务商
 */
export function fetchDeleteProvider(id: string) {
  return request({
    url: `/provider/${id}`,
    method: 'delete'
  });
}

/**
 * 更新服务商状态
 */
export function fetchUpdateProviderStatus(id: string, status: Api.Common.EnableStatus) {
  return request({
    url: `/provider/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 审核服务商
 */
export function fetchAuditProvider(id: string, data: { auditStatus: Api.Provider.AuditStatus; auditRemark?: string }) {
  return request({
    url: `/provider/${id}/audit`,
    method: 'put',
    data
  });
}

/**
 * 获取服务商统计
 */
export function fetchGetProviderStatistics(params?: { areaId?: string }) {
  return request<Api.Provider.Statistics>({
    url: '/provider/statistics',
    method: 'get',
    params
  });
}

/**
 * 获取服务商选择列表
 */
export function fetchGetProviderOptions(params?: { areaId?: string; serviceCategory?: string }) {
  return request<{ id: string; name: string }[]>({
    url: '/provider/options',
    method: 'get',
    params
  });
}
