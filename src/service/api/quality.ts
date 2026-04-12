import { request } from '../request';

/**
 * 获取质检列表
 */
export function fetchGetQualityCheckList(params?: Api.Quality.QualityCheckQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Quality.QualityCheck>>({
    url: '/api/quality-check/list',
    method: 'get',
    params
  });
}

/**
 * 获取质检详情
 */
export function fetchGetQualityCheck(id: string) {
  return request<Api.Quality.QualityCheck>({
    url: `/api/quality-check/${id}`,
    method: 'get'
  });
}

/**
 * 根据订单ID获取质检
 */
export function fetchGetQualityCheckByOrderId(orderId: string) {
  return request<Api.Quality.QualityCheck>({
    url: `/api/quality-check/order/${orderId}`,
    method: 'get'
  });
}

/**
 * 创建质检
 */
export function fetchCreateQualityCheck(data: Api.Quality.QualityCheckForm) {
  return request({
    url: '/api/quality-check',
    method: 'post',
    data
  });
}

/**
 * 更新质检
 */
export function fetchUpdateQualityCheck(id: string, data: Api.Quality.QualityCheckForm) {
  return request({
    url: `/api/quality-check/${id}`,
    method: 'put',
    data
  });
}

/**
 * 提交整改
 */
export function fetchSubmitRectify(id: string, data: { photos?: string[]; remark: string }) {
  return request({
    url: `/api/quality-check/${id}/rectify`,
    method: 'put',
    data
  });
}

/**
 * 复检
 */
export function fetchRecheck(id: string, data: { result: string; remark?: string }) {
  return request({
    url: `/api/quality-check/${id}/recheck`,
    method: 'put',
    data
  });
}

/**
 * 获取质检统计
 */
export function fetchGetQualityStatistics(params?: {
  areaId?: string;
  providerId?: string;
  startDate?: string;
  endDate?: string;
}) {
  return request<Api.Quality.Statistics>({
    url: '/api/quality-check/statistics',
    method: 'get',
    params
  });
}
