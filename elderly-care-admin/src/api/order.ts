import api from './index';
import type { Order, OrderQuery, PageResponse } from '../types';

export const getOrderList = (params: OrderQuery) => {
  return api.get<PageResponse<Order>>('/orders', { params });
};

export const getOrderById = (id: number) => {
  return api.get<Order>(`/orders/${id}`);
};

export const createOrder = (data: Record<string, unknown>) => {
  return api.post('/orders', data);
};

export const updateOrder = (id: number, data: Record<string, unknown>) => {
  return api.put(`/orders/${id}`, data);
};

export const deleteOrder = (id: number) => {
  return api.delete(`/orders/${id}`);
};

export const cancelOrder = (id: number, reason?: string) => {
  return api.post(`/orders/${id}/cancel`, { reason });
};

export const assignStaff = (orderId: number, staffId: number) => {
  return api.post(`/orders/${orderId}/assign`, { staffId });
};

export const startService = (orderId: number) => {
  return api.post(`/orders/${orderId}/start`);
};

export const completeService = (orderId: number, data?: Record<string, unknown>) => {
  return api.post(`/orders/${orderId}/complete`, data);
};

export const getOrderStats = (params?: Record<string, unknown>) => {
  return api.get('/orders/stats', { params });
};
