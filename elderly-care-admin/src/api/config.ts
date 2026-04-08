import api from './index';
import type { Config, PageResponse } from '../types';

export const getConfigList = (params?: Record<string, unknown>) => {
  return api.get<PageResponse<Config>>('/configs', { params });
};

export const getConfigById = (id: number) => {
  return api.get<Config>(`/configs/${id}`);
};

export const getConfigByCategory = (category: string) => {
  return api.get<Config[]>(`/configs/category/${category}`);
};

export const createConfig = (data: Record<string, unknown>) => {
  return api.post('/configs', data);
};

export const updateConfig = (id: number, data: Record<string, unknown>) => {
  return api.put(`/configs/${id}`, data);
};

export const deleteConfig = (id: number) => {
  return api.delete(`/configs/${id}`);
};

export const getServiceTypes = () => {
  return api.get<Config[]>('/configs/service-types');
};

export const getRegions = () => {
  return api.get<Config[]>('/configs/regions');
};

export const getCareLevels = () => {
  return api.get<Config[]>('/configs/care-levels');
};

export const getSubsidyTypes = () => {
  return api.get<Config[]>('/configs/subsidy-types');
};
