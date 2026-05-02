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
 * 根据服务日志ID获取质检（判断该日志是否已有质检）
 */
export function fetchGetQualityCheckByServiceLogId(serviceLogId: string) {
  return request<Api.Quality.QualityCheck | null>({
    url: `/api/quality-check/service-log/${serviceLogId}`,
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
 * 执行质检（质检员提交质检结论）
 * PUT /api/quality-check/{id}/inspect
 * QUALIFIED → 日志→APPROVED，订单→COMPLETED
 * UNQUALIFIED/NEED_RECTIFY → 开启整改流程
 */
export function fetchInspect(id: string, data: {
  checkScore?: number;
  checkMethod?: string;
  checkResult: string;
  checkRemark?: string;
  checkPhotos?: string;
  rectifyNotice?: string;
  rectifyDeadline?: string;
}) {
  return request({
    url: `/api/quality-check/${id}/inspect`,
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

/**
 * 批量删除质检
 */
export function fetchBatchDeleteQualityCheck(ids: string[]) {
  return request({
    url: '/api/quality-check/batch',
    method: 'post',
    data: ids
  });
}
