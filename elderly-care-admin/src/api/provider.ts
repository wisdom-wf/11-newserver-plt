import api from './index';
import type { Provider, ProviderQuery, PageResponse } from '../types';

export const getProviderList = (params: ProviderQuery) => {
  return api.get<PageResponse<Provider>>('/providers', { params });
};

export const getProviderById = (id: number) => {
  return api.get<Provider>(`/providers/${id}`);
};

export const createProvider = (data: Record<string, unknown>) => {
  return api.post('/providers', data);
};

export const updateProvider = (id: number, data: Record<string, unknown>) => {
  return api.put(`/providers/${id}`, data);
};

export const deleteProvider = (id: number) => {
  return api.delete(`/providers/${id}`);
};

export const approveProvider = (id: number, approved: boolean, reason?: string) => {
  return api.post(`/providers/${id}/approve`, { approved, reason });
};

export const enableProvider = (id: number) => {
  return api.post(`/providers/${id}/enable`);
};

export const disableProvider = (id: number) => {
  return api.post(`/providers/${id}/disable`);
};
