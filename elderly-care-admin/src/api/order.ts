import api from './index';
import type { Order, OrderQuery, PageResponse } from '../types';

export const getOrderList = (params: OrderQuery) => {
  return api.get<PageResponse<Order>>('/orders', { params });
};

export const getOrderById = (id: string) => {
  return api.get<Order>(`/orders/${id}`);
};

export const createOrder = (data: Record<string, unknown>) => {
  return api.post('/orders', data);
};

export const updateOrder = (id: string, data: Record<string, unknown>) => {
  return api.put(`/orders/${id}`, data);
};

export const deleteOrder = (id: string) => {
  return api.delete(`/orders/${id}`);
};

export const cancelOrder = (id: string, reason?: string) => {
  return api.put(`/orders/${id}/cancel`, { reason });
};

export const assignStaff = (orderId: string, staffId: string) => {
  return api.post(`/orders/${orderId}/dispatch`, { staffId });
};

export const startService = (id: string) => {
  return api.put(`/orders/${id}/start`, {});
};

export const completeService = (id: string, data?: Record<string, unknown>) => {
  return api.put(`/orders/${id}/complete`, data || {});
};

export const getOrderStats = (params?: Record<string, unknown>) => {
  return api.get('/orders/stats', { params });
};
