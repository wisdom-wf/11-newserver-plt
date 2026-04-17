import { request } from '../request';

/**
 * 获取订单列表
 */
export function fetchGetOrderList(params?: Api.Order.OrderQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Order.Order>>({
    url: '/api/orders',
    method: 'get',
    params
  });
}

/**
 * 获取订单详情
 */
export function fetchGetOrder(id: string) {
  return request<Api.Order.Order>({
    url: `/api/orders/${id}`,
    method: 'get'
  });
}

/**
 * 从预约创建订单
 */
export function fetchCreateOrderFromAppointment(appointmentId: string, data: { providerId: string; staffId?: string }) {
  return request({
    url: `/api/orders/appointment/${appointmentId}`,
    method: 'post',
    data
  });
}

/**
 * 创建订单
 */
export function fetchCreateOrder(data: Api.Order.OrderForm) {
  return request({
    url: '/api/orders',
    method: 'post',
    data
  });
}

/**
 * 更新订单
 */
export function fetchUpdateOrder(id: string, data: Api.Order.OrderForm) {
  return request({
    url: `/api/orders/${id}`,
    method: 'put',
    data
  });
}

/**
 * 派单
 */
export function fetchDispatchOrder(orderId: string, data: { staffId: string }) {
  return request({
    url: `/api/orders/${orderId}/dispatch`,
    method: 'post',
    data
  });
}

/**
 * 接单
 */
export function fetchAcceptOrder(orderId: string, data?: {
  staffId?: string;
  receiveComment?: string;
}) {
  return request({
    url: `/api/orders/${orderId}/receive`,
    method: 'put',
    data
  });
}

/**
 * 开始服务
 */
export function fetchStartOrder(orderId: string, data?: {
  longitude?: string;
  latitude?: string;
  checkInPhoto?: string;
}) {
  return request({
    url: `/api/orders/${orderId}/start`,
    method: 'put',
    data
  });
}

/**
 * 完成服务
 */
export function fetchCompleteOrder(orderId: string, data?: { actualFee?: number; selfPayFee?: number }) {
  return request({
    url: `/api/orders/${orderId}/complete`,
    method: 'put',
    data
  });
}

/**
 * 取消订单
 */
export function fetchCancelOrder(orderId: string, data: { reason: string }) {
  return request({
    url: `/api/orders/${orderId}/cancel`,
    method: 'put',
    data
  });
}

/**
 * 删除订单
 */
export function fetchDeleteOrder(orderId: string) {
  return request({
    url: `/api/orders/${orderId}`,
    method: 'delete'
  });
}

/**
 * 获取订单统计
 */
export function fetchGetOrderStatistics() {
  return request<Api.Order.Statistics>({
    url: '/api/orders/statistics',
    method: 'get'
  });
}
