import { request } from '../request';

/**
 * 获取驾驶舱概览统计
 */
export function fetchGetCockpitOverview(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.Overview>({
    url: '/cockpit/overview',
    method: 'get',
    params
  });
}

/**
 * 获取订单趋势
 */
export function fetchGetOrderTrend(params?: Api.Cockpit.CockpitQuery & { type?: 'day' | 'week' | 'month' }) {
  return request<Api.Cockpit.OrderTrend[]>({
    url: '/cockpit/orderTrend',
    method: 'get',
    params
  });
}

/**
 * 获取服务类别分布
 */
export function fetchGetServiceDistribution(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.ServiceDistribution[]>({
    url: '/cockpit/serviceDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取区域分布
 */
export function fetchGetAreaDistribution(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.AreaDistribution[]>({
    url: '/cockpit/areaDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取服务商排行
 */
export function fetchGetProviderRanking(
  params?: Api.Cockpit.CockpitQuery & { type?: 'order' | 'service' | 'rating' | 'amount'; limit?: number }
) {
  return request<Api.Cockpit.ProviderRanking[]>({
    url: '/cockpit/providerRanking',
    method: 'get',
    params
  });
}

/**
 * 获取服务人员排行
 */
export function fetchGetStaffRanking(
  params?: Api.Cockpit.CockpitQuery & { type?: 'order' | 'rating'; limit?: number }
) {
  return request<Api.Cockpit.StaffRanking[]>({
    url: '/cockpit/staffRanking',
    method: 'get',
    params
  });
}

/**
 * 获取满意度评价分布
 */
export function fetchGetSatisfactionDistribution(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.SatisfactionDistribution>({
    url: '/cockpit/satisfactionDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取质检结果分布
 */
export function fetchGetQualityDistribution(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.QualityDistribution>({
    url: '/cockpit/qualityDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取财务收支趋势
 */
export function fetchGetFinancialTrend(params?: Api.Cockpit.CockpitQuery & { type?: 'day' | 'week' | 'month' }) {
  return request<Api.Cockpit.FinancialTrend[]>({
    url: '/cockpit/financialTrend',
    method: 'get',
    params
  });
}

/**
 * 获取年龄段分布
 */
export function fetchGetAgeDistribution(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.AgeDistribution[]>({
    url: '/cockpit/ageDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取护理等级分布
 */
export function fetchGetCareLevelDistribution(params?: Api.Cockpit.CockpitQuery) {
  return request<Api.Cockpit.CareLevelDistribution[]>({
    url: '/cockpit/careLevelDistribution',
    method: 'get',
    params
  });
}

/**
 * 获取实时订单
 */
export function fetchGetRealtimeOrders(params?: { limit?: number }) {
  return request<Api.Cockpit.RealtimeOrder[]>({
    url: '/cockpit/realtimeOrders',
    method: 'get',
    params
  });
}

/**
 * 获取预警信息
 */
export function fetchGetWarnings(params?: { level?: Api.Cockpit.WarningLevel; limit?: number }) {
  return request<Api.Cockpit.Warning[]>({
    url: '/cockpit/warnings',
    method: 'get',
    params
  });
}
