import api from './index';
import type { QualityCheck, QualityCheckQuery, PageResponse } from '../types';

// 获取质检单列表
export const getQualityCheckList = (params: QualityCheckQuery) => {
  return api.get<PageResponse<QualityCheck>>('/quality-check/page', { params });
};

// 获取质检单详情
export const getQualityCheckDetail = (id: string) => {
  return api.get<QualityCheck>(`/quality-check/${id}`);
};

// 创建质检单
export const createQualityCheck = (data: Partial<QualityCheck>) => {
  return api.post<QualityCheck>('/quality-check', data);
};

// 更新质检单
export const updateQualityCheck = (id: string, data: Partial<QualityCheck>) => {
  return api.put<QualityCheck>(`/quality-check/${id}`, data);
};

// 删除质检单
export const deleteQualityCheck = (id: string) => {
  return api.delete<void>(`/quality-check/${id}`);
};

// 执行质检
export const performCheck = (id: string, data: Partial<QualityCheck>) => {
  return api.post<QualityCheck>(`/quality-check/${id}/perform`, data);
};

// 提交整改
export const submitRectify = (id: string, data: Partial<QualityCheck>) => {
  return api.post<QualityCheck>(`/quality-check/${id}/rectify`, data);
};

// 复检
export const recheckQuality = (id: string, data: Partial<QualityCheck>) => {
  return api.post<QualityCheck>(`/quality-check/${id}/recheck`, data);
};
