import { request } from '../request';

/**
 * 获取客户列表
 */
export function fetchGetElderList(params?: Api.Elder.ElderQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Elder.Elder>>({
    url: '/api/elders',
    method: 'get',
    params
  });
}

/**
 * 获取客户详情
 */
export function fetchGetElder(id: string) {
  return request<Api.Elder.Elder>({
    url: `/api/elders/${id}`,
    method: 'get'
  });
}

/**
 * 创建客户
 */
export function fetchCreateElder(data: Api.Elder.ElderForm) {
  return request({
    url: '/api/elders',
    method: 'post',
    data
  });
}

/**
 * 更新客户
 */
export function fetchUpdateElder(id: string, data: Api.Elder.ElderForm) {
  return request({
    url: `/api/elders/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除客户
 */
export function fetchDeleteElder(id: string) {
  return request({
    url: `/api/elders/${id}`,
    method: 'delete'
  });
}

/**
 * 批量删除客户
 */
export function fetchBatchDeleteElder(ids: string[]) {
  return request({
    url: '/api/elders/batch',
    method: 'post',
    data: ids
  });
}

/**
 * 获取最近更新的客户列表（用于健康卡片展示）
 */
export function fetchGetRecentUpdatedElders(limit: number = 10) {
  return request<Api.Elder.ElderHealthCard[]>({
    url: '/api/elders/recent',
    method: 'get',
    params: { limit }
  });
}

/**
 * 更新客户状态
 */
export function fetchUpdateElderStatus(id: string, status: Api.Common.EnableStatus) {
  return request({
    url: `/api/elders/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 获取客户统计
 */
export function fetchGetElderStatistics(params?: { areaId?: string; communityId?: string }) {
  return request<Api.Elder.Statistics>({
    url: '/api/statistics/elder',
    method: 'get',
    params
  });
}

/**
 * 获取客户选择列表
 */
export function fetchGetElderOptions(params?: { areaId?: string; communityId?: string }) {
  return request<{ id: string; name: string; phone?: string }[]>({
    url: '/api/elders/options',
    method: 'get',
    params
  });
}

/**
 * 通过身份证号获取客户信息
 */
export function fetchGetElderByIdCard(idCard: string) {
  return request<Api.Elder.Elder>({
    url: `/api/elders/idCard/${idCard}`,
    method: 'get'
  });
}

// ==================== 健康测量记录 ====================

/**
 * 添加健康测量记录
 */
export function fetchAddHealthMeasurement(elderId: string, data: Api.Elder.HealthMeasurementForm) {
  return request<Api.Elder.HealthMeasurement>({
    url: `/api/elders/${elderId}/measurements`,
    method: 'post',
    data
  });
}

/**
 * 批量添加健康测量记录
 */
export function fetchAddHealthMeasurements(elderId: string, data: Api.Elder.HealthMeasurementForm[]) {
  return request<Api.Elder.HealthMeasurement[]>({
    url: `/api/elders/${elderId}/measurements/batch`,
    method: 'post',
    data
  });
}

/**
 * 获取健康测量历史
 */
export function fetchGetHealthMeasurementHistory(
  elderId: string,
  params?: { measurementType?: string; limit?: number }
) {
  return request<Api.Elder.HealthMeasurement[]>({
    url: `/api/elders/${elderId}/measurements`,
    method: 'get',
    params
  });
}

/**
 * 获取客户最新一次测量记录（指定类型）
 */
export function fetchGetLatestMeasurement(elderId: string, measurementType: string) {
  return request<Api.Elder.HealthMeasurement>({
    url: `/api/elders/${elderId}/measurements/latest`,
    method: 'get',
    params: { measurementType }
  });
}

/**
 * 获取客户最新测量记录（所有类型）
 */
export function fetchGetLatestMeasurements(elderId: string) {
  return request<Api.Elder.HealthMeasurement[]>({
    url: `/api/elders/${elderId}/measurements/latest/all`,
    method: 'get'
  });
}

/**
 * 获取健康测量统计数据
 */
export function fetchGetMeasurementStatistics(elderId: string, measurementType: string) {
  return request<Api.Elder.HealthMeasurementStatistics>({
    url: `/api/elders/${elderId}/measurements/statistics`,
    method: 'get',
    params: { measurementType }
  });
}

/**
 * 获取客户所有类型测量的统计数据
 */
export function fetchGetAllMeasurementStatistics(elderId: string) {
  return request<Api.Elder.HealthMeasurementStatistics[]>({
    url: `/api/elders/${elderId}/measurements/statistics/all`,
    method: 'get'
  });
}

/**
 * 删除测量记录
 */
export function fetchDeleteMeasurement(measurementId: string) {
  return request({
    url: `/api/elders/measurements/${measurementId}`,
    method: 'delete'
  });
}

// ==================== 健康报告 ====================

/**
 * 生成健康报告
 */
export function fetchGenerateHealthReport(elderId: string, data: Api.Elder.HealthReportGenerate) {
  return request<Api.Elder.HealthReport>({
    url: `/api/elders/${elderId}/health-reports`,
    method: 'post',
    data
  });
}

/**
 * 获取客户健康报告列表
 */
export function fetchGetHealthReportList(elderId: string) {
  return request<Api.Elder.HealthReport[]>({
    url: `/api/elders/${elderId}/health-reports`,
    method: 'get'
  });
}

/**
 * 获取健康报告详情
 */
export function fetchGetHealthReport(reportId: string) {
  return request<Api.Elder.HealthReportVO>({
    url: `/api/elders/health-reports/${reportId}`,
    method: 'get'
  });
}

/**
 * 下载健康报告PDF
 */
export function fetchDownloadHealthReportPdf(reportId: string) {
  return request<Blob>({
    url: `/api/elders/health-reports/${reportId}/pdf`,
    method: 'get',
    responseType: 'blob'
  });
}

/**
 * 删除健康报告
 */
export function fetchDeleteHealthReport(reportId: string) {
  return request({
    url: `/api/elders/health-reports/${reportId}`,
    method: 'delete'
  });
}

// ==================== AI健康建议 ====================

/**
 * 获取护理建议
 */
export function fetchGetCareSuggestions(elderId: string) {
  return request<Api.Elder.CareSuggestionVO>({
    url: `/api/elders/${elderId}/care-suggestions`,
    method: 'get'
  });
}

/**
 * 获取就医建议
 */
export function fetchGetMedicalSuggestions(elderId: string) {
  return request<Api.Elder.MedicalSuggestionVO>({
    url: `/api/elders/${elderId}/medical-suggestions`,
    method: 'get'
  });
}
