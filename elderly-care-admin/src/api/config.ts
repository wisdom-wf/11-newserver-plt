import api from './index';
import type { Config, PageResponse } from '../types';

export const getConfigList = (params?: Record<string, unknown>) => {
  return api.get<PageResponse<Config>>('/config/dict-types', { params });
};

export const getConfigById = (id: string) => {
  return api.get<Config>(`/config/dict-types/${id}`);
};

export const getConfigByCategory = (category: string) => {
  return api.get<Config[]>(`/config/dict-items/${category}`);
};

export const createConfig = (data: Record<string, unknown>) => {
  return api.post('/config/dict-types', data);
};

export const updateConfig = (id: string, data: Record<string, unknown>) => {
  return api.put(`/config/dict-types/${id}`, data);
};

export const deleteConfig = (id: string) => {
  return api.delete(`/config/dict-types/${id}`);
};

export const getServiceTypes = () => {
  return api.get<Config[]>('/config/service-types');
};

export const getRegions = () => {
  return api.get<Config[]>('/config/areas');
};

export const getCareLevels = () => {
  return api.get<Config[]>('/config/care-levels');
};

export const getSubsidyTypes = () => {
  return api.get<Config[]>('/config/subsidy-types');
};
