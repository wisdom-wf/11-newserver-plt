import api from './index';
import type { Financial, FinancialQuery, PageResponse } from '../types';

export const getFinancialList = (params: FinancialQuery) => {
  return api.get<PageResponse<Financial>>('/financial/settlements', { params });
};

export const getFinancialById = (id: string) => {
  return api.get<Financial>(`/financial/settlements/${id}`);
};

export const createFinancial = (data: Record<string, unknown>) => {
  return api.post('/financial/settlements/calculate', data);
};

export const updateFinancial = (id: string, data: Record<string, unknown>) => {
  return api.put(`/financial/settlements/${id}`, data);
};

export const deleteFinancial = (id: string) => {
  return api.delete(`/financial/settlements/${id}`);
};

export const settleOrder = (orderId: number) => {
  return api.post(`/financial/settlements/calculate`, { orderId });
};

export const batchSettle = (orderIds: number[]) => {
  return api.post('/financial/settlements/batch', { orderIds });
};

export const confirmSettlement = (id: string) => {
  return api.post(`/financial/settlements/${id}/confirm`);
};

export const getFinancialStats = (params?: Record<string, unknown>) => {
  return api.get('/financial/reports', { params });
};

export const exportFinancial = (params?: Record<string, unknown>) => {
  return api.get('/financial/reports/export', { params, responseType: 'blob' });
};
