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
 * 前端字段名与后端不一致，需要映射:
 * serviceScore -> rating (总体评分)
 * skillScore -> qualityScore (质量评分)
 * punctualityScore -> efficiencyScore (效率评分)
 */
export function fetchCreateEvaluation(data: Api.Evaluation.EvaluationForm) {
  // 转换为后端期望的字段名
  const backendData = {
    orderId: data.orderId,
    staffId: data.staffId || '', // 后端需要staffId，前端form可能没有
    rating: data.serviceScore, // 总体评分
    attitudeScore: data.attitudeScore,
    qualityScore: data.skillScore, // 质量评分
    efficiencyScore: data.punctualityScore, // 效率评分
    environmentScore: data.environmentScore, // 环境评分
    content: data.content,
    tags: data.tags,
    anonymous: data.anonymous
  };
  return request({
    url: '/api/evaluations',
    method: 'post',
    data: backendData
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
