import api from './index';
import type { Financial, FinancialQuery, PageResponse } from '../types';

export const getFinancialList = (params: FinancialQuery) => {
  return api.get<PageResponse<Financial>>('/financial', { params });
};

export const getFinancialById = (id: number) => {
  return api.get<Financial>(`/financial/${id}`);
};

export const createFinancial = (data: Record<string, unknown>) => {
  return api.post('/financial', data);
};

export const updateFinancial = (id: number, data: Record<string, unknown>) => {
  return api.put(`/financial/${id}`, data);
};

export const deleteFinancial = (id: number) => {
  return api.delete(`/financial/${id}`);
};

export const settleOrder = (orderId: number) => {
  return api.post(`/financial/settle/${orderId}`);
};

export const batchSettle = (orderIds: number[]) => {
  return api.post('/financial/batch-settle', { orderIds });
};

export const confirmSettlement = (id: number) => {
  return api.post(`/financial/${id}/confirm`);
};

export const getFinancialStats = (params?: Record<string, unknown>) => {
  return api.get('/financial/stats', { params });
};

export const exportFinancial = (params?: Record<string, unknown>) => {
  return api.get('/financial/export', { params, responseType: 'blob' });
};
