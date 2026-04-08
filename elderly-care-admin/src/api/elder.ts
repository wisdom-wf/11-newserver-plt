import api from './index';
import type { Elder, ElderQuery, PageResponse } from '../types';

export const getElderList = (params: ElderQuery) => {
  return api.get<PageResponse<Elder>>('/elders', { params });
};

export const getElderById = (id: number) => {
  return api.get<Elder>(`/elders/${id}`);
};

export const createElder = (data: Record<string, unknown>) => {
  return api.post('/elders', data);
};

export const updateElder = (id: number, data: Record<string, unknown>) => {
  return api.put(`/elders/${id}`, data);
};

export const deleteElder = (id: number) => {
  return api.delete(`/elders/${id}`);
};

export const enableElder = (id: number) => {
  return api.post(`/elders/${id}/enable`);
};

export const disableElder = (id: number) => {
  return api.post(`/elders/${id}/disable`);
};

export const getElderStats = (params?: Record<string, unknown>) => {
  return api.get('/elders/stats', { params });
};

export const importElders = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/elders/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const exportElders = (params?: Record<string, unknown>) => {
  return api.get('/elders/export', { params, responseType: 'blob' });
};
