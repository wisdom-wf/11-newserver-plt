import { request } from '../request';

/**
 * 生成公开访问Token
 */
export function fetchGeneratePublicToken(params?: { type?: string; expiresInSeconds?: number }) {
  return request<{ token: string; expiresInSeconds: number }>({
    url: '/api/public/token',
    method: 'post',
    params
  });
}

/**
 * 验证公开Token
 */
export function fetchValidatePublicToken(token: string) {
  return request<boolean>({
    url: '/api/public/token/validate',
    method: 'get',
    params: { token }
  });
}

/**
 * 撤销公开Token
 */
export function fetchRevokePublicToken(token: string) {
  return request<void>({
    url: '/api/public/token',
    method: 'delete',
    params: { token }
  });
}

/**
 * 获取公开驾驶舱概览
 */
export function fetchGetPublicCockpitOverview(params?: { token?: string }) {
  return request<Api.Cockpit.Overview>({
    url: '/api/public/cockpit/overview',
    method: 'get',
    params
  });
}

/**
 * 获取公开订单趋势
 */
export function fetchGetPublicOrderTrend(params?: { token?: string; type?: 'day' | 'week' | 'month' }) {
  return request<Api.Cockpit.OrderTrend[]>({
    url: '/api/public/cockpit/orderTrend',
    method: 'get',
    params
  });
}

/**
 * 获取公开服务商排行
 */
export function fetchGetPublicProviderRanking(params?: { token?: string; type?: string; limit?: number }) {
  return request<Api.Cockpit.ProviderRanking[]>({
    url: '/api/public/cockpit/providerRanking',
    method: 'get',
    params
  });
}

/**
 * 获取公开服务类型分布
 */
export function fetchGetPublicServiceDistribution(params?: { token?: string }) {
  return request<Api.Cockpit.ServiceDistribution[]>({
    url: '/api/public/cockpit/serviceDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取公开区域分布
 */
export function fetchGetPublicAreaDistribution(params?: { token?: string }) {
  return request<Api.Cockpit.AreaDistribution[]>({
    url: '/api/public/cockpit/areaDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取公开年龄段分布
 */
export function fetchGetPublicAgeDistribution(params?: { token?: string }) {
  return request<Api.Cockpit.AgeDistribution[]>({
    url: '/api/public/cockpit/ageDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取公开护理等级分布
 */
export function fetchGetPublicCareLevelDistribution(params?: { token?: string }) {
  return request<Api.Cockpit.CareLevelDistribution[]>({
    url: '/api/public/cockpit/careLevelDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取公开实时订单
 */
export function fetchGetPublicRealtimeOrders(params?: { token?: string; limit?: number }) {
  return request<Api.Cockpit.RealtimeOrder[]>({
    url: '/api/public/cockpit/realtimeOrders',
    method: 'get',
    params
  });
}
