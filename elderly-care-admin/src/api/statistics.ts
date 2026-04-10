import api from './index';
import type { DashboardStats, StatisticsQuery } from '../types';

export const getDashboardStats = () => {
  return api.get<DashboardStats>('/statistics/dashboard');
};

export const getOrderTrend = (params?: StatisticsQuery) => {
  // Backend uses /statistics/order which returns OrderStatisticsVO
  // We need to extract trend data from it
  return api.get<{ date: string; value: number }[]>('/statistics/order', { params });
};

export const getRevenueTrend = (params?: StatisticsQuery) => {
  // Backend uses /statistics/financial which returns FinancialStatisticsVO
  return api.get<{ date: string; value: number }[]>('/statistics/financial', { params });
};

export const getProviderStats = (params?: StatisticsQuery) => {
  return api.get<{ label: string; value: number }[]>('/statistics/provider', { params });
};

export const getStaffStats = (params?: StatisticsQuery) => {
  // Backend doesn't have /statistics/staff, use a workaround
  return api.get<{ label: string; value: number }[]>('/statistics/order', { params });
};

export const getServiceTypeStats = (params?: StatisticsQuery) => {
  // Backend doesn't have this, derive from order stats
  return api.get<{ label: string; value: number }[]>('/statistics/order', { params });
};

export const getRegionStats = (params?: StatisticsQuery) => {
  // Backend doesn't have region stats
  return api.get<{ label: string; value: number }[]>('/statistics/provider', { params });
};

export const getElderStats = (params?: StatisticsQuery) => {
  return api.get<{ label: string; value: number }[]>('/statistics/elder', { params });
};
