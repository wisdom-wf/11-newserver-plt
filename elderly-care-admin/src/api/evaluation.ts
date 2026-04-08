import api from './index';
import type { Evaluation, EvaluationQuery, PageResponse } from '../types';

export const getEvaluationList = (params: EvaluationQuery) => {
  return api.get<PageResponse<Evaluation>>('/evaluations', { params });
};

export const getEvaluationById = (id: number) => {
  return api.get<Evaluation>(`/evaluations/${id}`);
};

export const createEvaluation = (data: Record<string, unknown>) => {
  return api.post('/evaluations', data);
};

export const updateEvaluation = (id: number, data: Record<string, unknown>) => {
  return api.put(`/evaluations/${id}`, data);
};

export const replyEvaluation = (id: number, reply: string) => {
  return api.post(`/evaluations/${id}/reply`, { reply });
};

export const getEvaluationStats = (params?: Record<string, unknown>) => {
  return api.get('/evaluations/stats', { params });
};

export const exportEvaluations = (params?: Record<string, unknown>) => {
  return api.get('/evaluations/export', { params, responseType: 'blob' });
};
