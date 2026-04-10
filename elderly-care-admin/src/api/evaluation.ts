import api from './index';
import type { Evaluation, EvaluationQuery, PageResponse } from '../types';

export const getEvaluationList = (params: EvaluationQuery) => {
  return api.get<PageResponse<Evaluation>>('/evaluations', { params });
};

export const getEvaluationById = (id: string) => {
  return api.get<Evaluation>(`/evaluations/${id}`);
};

export const createEvaluation = (data: Record<string, unknown>) => {
  return api.post('/evaluations', data);
};

export const updateEvaluation = (id: string, data: Record<string, unknown>) => {
  return api.put(`/evaluations/${id}`, data);
};

export const replyEvaluation = (id: string, reply: string) => {
  return api.put(`/evaluations/${id}/reply`, reply);
};

export const getEvaluationStats = (params?: Record<string, unknown>) => {
  return api.get('/evaluations/statistics', { params });
};

export const exportEvaluations = () => {
  // Backend doesn't have export endpoint, return empty promise
  return Promise.reject(new Error('Export not implemented'));
};
