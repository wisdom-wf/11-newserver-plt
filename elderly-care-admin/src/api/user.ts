import api from './index';
import type { UserInfo, Role, Permission, PageResponse } from '../types';

export const getUserList = (params: Record<string, unknown>) => {
  return api.get<PageResponse<UserInfo>>('/users', { params });
};

export const getUserById = (id: number) => {
  return api.get<UserInfo>(`/users/${id}`);
};

export const createUser = (data: Record<string, unknown>) => {
  return api.post('/users', data);
};

export const updateUser = (id: number, data: Record<string, unknown>) => {
  return api.put(`/users/${id}`, data);
};

export const deleteUser = (id: number) => {
  return api.delete(`/users/${id}`);
};

export const resetPassword = (id: number) => {
  return api.post(`/users/${id}/reset-password`);
};

export const getRoleList = (params?: Record<string, unknown>) => {
  return api.get<PageResponse<Role>>('/roles', { params });
};

export const getRoleById = (id: number) => {
  return api.get<Role>(`/roles/${id}`);
};

export const createRole = (data: Record<string, unknown>) => {
  return api.post('/roles', data);
};

export const updateRole = (id: number, data: Record<string, unknown>) => {
  return api.put(`/roles/${id}`, data);
};

export const deleteRole = (id: number) => {
  return api.delete(`/roles/${id}`);
};

export const getPermissionList = () => {
  return api.get<Permission[]>('/permissions');
};

export const getPermissionTree = () => {
  return api.get<Permission[]>('/permissions/tree');
};
