import { request } from '../request';

/**
 * 获取评价列表
 */
export function fetchGetEvaluationList(params?: Api.Evaluation.EvaluationQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Evaluation.Evaluation>>({
    url: '/api/evaluations',
    method: 'get',
    params
  });
}

/**
 * 获取评价详情
 */
export function fetchGetEvaluation(id: string) {
  return request<Api.Evaluation.Evaluation>({
    url: `/api/evaluations/${id}`,
    method: 'get'
  });
}

/**
 * 根据订单ID获取评价
 */
export function fetchGetEvaluationByOrderId(orderId: string) {
  return request<Api.Evaluation.Evaluation>({
    url: `/api/evaluations/order/${orderId}`,
    method: 'get'
  });
}

/**
 * 创建评价
 */
export function fetchCreateEvaluation(data: Api.Evaluation.EvaluationForm) {
  return request({
    url: '/api/evaluations',
    method: 'post',
    data
  });
}

/**
 * 回复评价
 */
export function fetchReplyEvaluation(id: string, data: { reply: string }) {
  return request({
    url: `/api/evaluations/${id}/reply`,
    method: 'put',
    data
  });
}

/**
 * 获取评价统计
 */
export function fetchGetEvaluationStatistics(params?: {
  areaId?: string;
  providerId?: string;
  startDate?: string;
  endDate?: string;
}) {
  return request<Api.Evaluation.Statistics>({
    url: '/api/evaluations/statistics',
    method: 'get',
    params
  });
}
