import { request } from '../request';

/**
 * 获取评价列表
 */
export function fetchGetEvaluationList(params?: Api.Evaluation.EvaluationQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Evaluation.Evaluation>>({
    url: '/evaluation/list',
    method: 'get',
    params
  });
}

/**
 * 获取评价详情
 */
export function fetchGetEvaluation(id: string) {
  return request<Api.Evaluation.Evaluation>({
    url: `/evaluation/${id}`,
    method: 'get'
  });
}

/**
 * 根据订单ID获取评价
 */
export function fetchGetEvaluationByOrderId(orderId: string) {
  return request<Api.Evaluation.Evaluation>({
    url: `/evaluation/order/${orderId}`,
    method: 'get'
  });
}

/**
 * 创建评价
 */
export function fetchCreateEvaluation(data: Api.Evaluation.EvaluationForm) {
  return request({
    url: '/evaluation',
    method: 'post',
    data
  });
}

/**
 * 回复评价
 */
export function fetchReplyEvaluation(id: string, data: { reply: string }) {
  return request({
    url: `/evaluation/${id}/reply`,
    method: 'put',
    data
  });
}

/**
 * 获取评价统计
 */
export function fetchGetEvaluationStatistics(params?: { areaId?: string; providerId?: string; startDate?: string; endDate?: string }) {
  return request<Api.Evaluation.Statistics>({
    url: '/evaluation/statistics',
    method: 'get',
    params
  });
}
