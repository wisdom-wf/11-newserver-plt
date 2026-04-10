import api from './index';
import type { Staff, StaffQuery, PageResponse } from '../types';

export const getStaffList = (params: StaffQuery) => {
  return api.get<PageResponse<Staff>>('/staff', { params });
};

export const getStaffById = (id: string) => {
  return api.get<Staff>(`/staff/${id}`);
};

export const createStaff = (data: Record<string, unknown>) => {
  return api.post('/staff', data);
};

export const updateStaff = (id: string, data: Record<string, unknown>) => {
  return api.put(`/staff/${id}`, data);
};

export const deleteStaff = (id: string) => {
  return api.delete(`/staff/${id}`);
};

export const enableStaff = (id: string) => {
  return api.put(`/staff/${id}/status`, { status: 1 });
};

export const disableStaff = (id: string) => {
  return api.put(`/staff/${id}/status`, { status: 0 });
};

export const getStaffStats = (params?: Record<string, unknown>) => {
  return api.get('/staff/stats', { params });
};
