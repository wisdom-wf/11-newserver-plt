import { request } from '../request';

/**
 * 获取服务日志列表
 */
export function fetchGetServiceLogList(params?: Api.ServiceLog.ServiceLogQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.ServiceLog.ServiceLog>>({
    url: '/api/service-log/list',
    method: 'get',
    params
  });
}

/**
 * 获取服务日志详情
 */
export function fetchGetServiceLog(id: string) {
  return request<Api.ServiceLog.ServiceLog>({
    url: `/api/service-log/${id}`,
    method: 'get'
  });
}

/**
 * 根据订单ID获取服务日志
 */
export function fetchGetServiceLogByOrderId(orderId: string) {
  return request<Api.ServiceLog.ServiceLog>({
    url: `/api/service-log/order/${orderId}`,
    method: 'get'
  });
}

/**
 * 提交服务日志
 */
export function fetchSubmitServiceLog(data: Api.ServiceLog.ServiceLogForm) {
  return request({
    url: '/api/service-log',
    method: 'post',
    data
  });
}

/**
 * 更新服务日志
 */
export function fetchUpdateServiceLog(id: string, data: Api.ServiceLog.ServiceLogForm) {
  return request({
    url: `/api/service-log/${id}`,
    method: 'put',
    data
  });
}

/**
 * 上报异常
 */
export function fetchReportAnomaly(
  id: string,
  data: { anomalyType: string; anomalyDesc: string; anomalyPhotos?: string[] }
) {
  return request({
    url: `/api/service-log/${id}/anomaly`,
    method: 'put',
    data
  });
}

/**
 * 提交审核
 */
export function fetchSubmitServiceLogForReview(id: string, remarks?: string) {
  return request({
    url: `/api/service-log/${id}/submit-review`,
    method: 'put',
    params: remarks ? { remarks } : undefined
  });
}

/**
 * 审核服务日志
 */
export function fetchReviewServiceLog(id: string, result: 'APPROVED' | 'REJECTED', reviewComment?: string) {
  return request({
    url: `/api/service-log/${id}/review`,
    method: 'put',
    params: { result, reviewComment }
  });
}

/**
 * 删除服务日志
 */
export function fetchDeleteServiceLog(id: string) {
  return request({
    url: `/api/service-log/${id}`,
    method: 'delete'
  });
}

/**
 * 批量删除服务日志
 */
export function fetchBatchDeleteServiceLog(ids: string[]) {
  return request({
    url: '/api/service-log/batch',
    method: 'delete',
    data: ids
  });
}

/**
 * 获取服务日志统计
 */
export function fetchGetServiceLogStatistics(params?: {
  areaId?: string;
  providerId?: string;
  staffId?: string;
  startDate?: string;
  endDate?: string;
}) {
  return request<Api.ServiceLog.Statistics>({
    url: '/api/service-log/statistics',
    method: 'get',
    params
  });
}

/**
 * 出发
 */
export function fetchDeparture(id: string, data: { location?: string; remark?: string }) {
  return request({
    url: `/api/service-log/${id}/departure`,
    method: 'put',
    data
  });
}

/**
 * 签到
 */
export function fetchSignIn(id: string, data: { location: string; photos?: string[]; remark?: string }) {
  return request({
    url: `/api/service-log/${id}/sign-in`,
    method: 'put',
    data
  });
}

/**
 * 签退
 */
export function fetchSignOut(
  id: string,
  data: { location: string; photos?: string[]; actualDuration?: number; remark?: string }
) {
  return request({
    url: `/api/service-log/${id}/sign-out`,
    method: 'put',
    data
  });
}
