import api from './index';
import type { Staff, StaffQuery, PageResponse } from '../types';

export const getStaffList = (params: StaffQuery) => {
  return api.get<PageResponse<Staff>>('/staff', { params });
};

export const getStaffById = (id: number) => {
  return api.get<Staff>(`/staff/${id}`);
};

export const createStaff = (data: Record<string, unknown>) => {
  return api.post('/staff', data);
};

export const updateStaff = (id: number, data: Record<string, unknown>) => {
  return api.put(`/staff/${id}`, data);
};

export const deleteStaff = (id: number) => {
  return api.delete(`/staff/${id}`);
};

export const enableStaff = (id: number) => {
  return api.post(`/staff/${id}/enable`);
};

export const disableStaff = (id: number) => {
  return api.post(`/staff/${id}/disable`);
};

export const getStaffStats = (params?: Record<string, unknown>) => {
  return api.get('/staff/stats', { params });
};
