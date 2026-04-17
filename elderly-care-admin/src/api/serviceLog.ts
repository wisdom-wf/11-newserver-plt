import api from './index';
import type { ServiceLog, ServiceLogQuery, PageResponse } from '../types';

// 获取服务日志列表
export const getServiceLogList = (params: ServiceLogQuery) => {
  return api.get<PageResponse<ServiceLog>>('/service-log/page', { params });
};

// 获取服务日志详情
export const getServiceLogDetail = (id: string) => {
  return api.get<ServiceLog>(`/service-log/${id}`);
};

// 创建服务日志
export const createServiceLog = (data: Partial<ServiceLog>) => {
  return api.post<ServiceLog>('/service-log', data);
};

// 更新服务日志
export const updateServiceLog = (id: string, data: Partial<ServiceLog>) => {
  return api.put<ServiceLog>(`/service-log/${id}`, data);
};

// 删除服务日志
export const deleteServiceLog = (id: string) => {
  return api.delete<void>(`/service-log/${id}`);
};

// 提交服务日志
export const submitServiceLog = (id: string, data: Partial<ServiceLog>) => {
  return api.post<ServiceLog>(`/service-log/${id}/submit`, data);
};

// 审核服务日志
export const auditServiceLog = (id: string, auditStatus: string, remark?: string) => {
  return api.post<ServiceLog>(`/service-log/${id}/audit`, { auditStatus, remark });
};
