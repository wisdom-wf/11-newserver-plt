import { request } from '../request';

/**
 * 获取订单列表
 */
export function fetchGetOrderList(params?: Api.Order.OrderQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Order.Order>>({
    url: '/order/list',
    method: 'get',
    params
  });
}

/**
 * 获取订单详情
 */
export function fetchGetOrder(id: string) {
  return request<Api.Order.Order>({
    url: `/order/${id}`,
    method: 'get'
  });
}

/**
 * 从预约创建订单
 */
export function fetchCreateOrderFromAppointment(appointmentId: string, data: { providerId: string; staffId?: string }) {
  return request({
    url: `/order/appointment/${appointmentId}`,
    method: 'post',
    data
  });
}

/**
 * 创建订单
 */
export function fetchCreateOrder(data: Api.Order.OrderForm) {
  return request({
    url: '/order',
    method: 'post',
    data
  });
}

/**
 * 更新订单
 */
export function fetchUpdateOrder(id: string, data: Api.Order.OrderForm) {
  return request({
    url: `/order/${id}`,
    method: 'put',
    data
  });
}

/**
 * 分配订单
 */
export function fetchAssignOrder(orderId: string, data: { staffId: string }) {
  return request({
    url: `/order/${orderId}/assign`,
    method: 'put',
    data
  });
}

/**
 * 接单
 */
export function fetchAcceptOrder(orderId: string) {
  return request({
    url: `/order/${orderId}/accept`,
    method: 'put'
  });
}

/**
 * 开始服务
 */
export function fetchStartOrder(orderId: string) {
  return request({
    url: `/order/${orderId}/start`,
    method: 'put'
  });
}

/**
 * 完成服务
 */
export function fetchCompleteOrder(orderId: string, data?: { actualFee?: number; selfPayFee?: number }) {
  return request({
    url: `/order/${orderId}/complete`,
    method: 'put',
    data
  });
}

/**
 * 取消订单
 */
export function fetchCancelOrder(orderId: string, data: { reason: string }) {
  return request({
    url: `/order/${orderId}/cancel`,
    method: 'put',
    data
  });
}

/**
 * 删除订单
 */
export function fetchDeleteOrder(orderId: string) {
  return request({
    url: `/order/${orderId}`,
    method: 'delete'
  });
}

/**
 * 获取订单统计
 */
export function fetchGetOrderStatistics(params?: { areaId?: string; providerId?: string; startDate?: string; endDate?: string }) {
  return request<Api.Order.Statistics>({
    url: '/order/statistics',
    method: 'get',
    params
  });
}
