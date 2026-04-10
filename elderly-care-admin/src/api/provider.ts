import api from './index';
import type { Provider, ProviderQuery, PageResponse } from '../types';

export const getProviderList = (params: ProviderQuery) => {
  return api.get<PageResponse<Provider>>('/providers', { params });
};

export const getProviderById = (id: string) => {
  return api.get<Provider>(`/providers/${id}`);
};

export const createProvider = (data: Record<string, unknown>) => {
  return api.post('/providers', data);
};

export const updateProvider = (id: string, data: Record<string, unknown>) => {
  return api.put(`/providers/${id}`, data);
};

export const deleteProvider = (id: string) => {
  return api.delete(`/providers/${id}`);
};

export const approveProvider = (id: string, approved: boolean, reason?: string) => {
  return api.put(`/providers/${id}/audit`, { approved, reason });
};

export const enableProvider = (id: string) => {
  return api.put(`/providers/${id}/status`, { status: 1 });
};

export const disableProvider = (id: string) => {
  return api.put(`/providers/${id}/status`, { status: 0 });
};
