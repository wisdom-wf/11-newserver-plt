import api from './index';
import type { LoginRequest, LoginResponse } from '../types';

export const login = (data: LoginRequest) => {
  return api.post<LoginResponse>('/auth/login', data);
};

export const logout = () => {
  return api.post('/auth/logout');
};

export const getCurrentUser = () => {
  return api.get('/auth/current');
};

export const updatePassword = (oldPassword: string, newPassword: string) => {
  return api.post('/auth/password', { oldPassword, newPassword });
};
