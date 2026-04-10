import { request } from '../request';

/**
 * 获取服务日志列表
 */
export function fetchGetServiceLogList(params?: Api.ServiceLog.ServiceLogQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.ServiceLog.ServiceLog>>({
    url: '/service-log/list',
    method: 'get',
    params
  });
}

/**
 * 获取服务日志详情
 */
export function fetchGetServiceLog(id: string) {
  return request<Api.ServiceLog.ServiceLog>({
    url: `/service-log/${id}`,
    method: 'get'
  });
}

/**
 * 根据订单ID获取服务日志
 */
export function fetchGetServiceLogByOrderId(orderId: string) {
  return request<Api.ServiceLog.ServiceLog>({
    url: `/service-log/order/${orderId}`,
    method: 'get'
  });
}

/**
 * 提交服务日志
 */
export function fetchSubmitServiceLog(data: Api.ServiceLog.ServiceLogForm) {
  return request({
    url: '/service-log',
    method: 'post',
    data
  });
}

/**
 * 更新服务日志
 */
export function fetchUpdateServiceLog(id: string, data: Api.ServiceLog.ServiceLogForm) {
  return request({
    url: `/service-log/${id}`,
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
    url: `/service-log/${id}/anomaly`,
    method: 'put',
    data
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
    url: '/service-log/statistics',
    method: 'get',
    params
  });
}
