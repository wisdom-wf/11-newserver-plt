import api from './index';
import type { DashboardStats, StatisticsQuery, ChartData } from '../types';

export const getDashboardStats = () => {
  return api.get<DashboardStats>('/statistics/dashboard');
};

export const getOrderTrend = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/order-trend', { params });
};

export const getRevenueTrend = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/revenue-trend', { params });
};

export const getProviderStats = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/providers', { params });
};

export const getStaffStats = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/staff', { params });
};

export const getServiceTypeStats = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/service-types', { params });
};

export const getRegionStats = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/regions', { params });
};

export const getElderStats = (params?: StatisticsQuery) => {
  return api.get<ChartData[]>('/statistics/elders', { params });
};
