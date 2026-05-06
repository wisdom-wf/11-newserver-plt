import { request } from '../request';

/**
 * 获取预约列表
 */
export function fetchGetAppointmentList(params?: Api.Appointment.AppointmentQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Appointment.Appointment>>({
    url: '/api/appointment/list',
    method: 'get',
    params
  });
}

/**
 * 获取预约详情
 */
export function fetchGetAppointment(id: string) {
  return request<Api.Appointment.Appointment>({
    url: `/api/appointment/${id}`,
    method: 'get'
  });
}

/**
 * 创建预约
 */
export function fetchCreateAppointment(data: Api.Appointment.AppointmentForm) {
  return request<string>({
    url: '/api/appointment',
    method: 'post',
    data
  });
}

/**
 * 确认预约
 */
export function fetchConfirmAppointment(id: string, data: { providerId: string; appointmentTime: string }) {
  return request({
    url: `/api/appointment/${id}/confirm`,
    method: 'put',
    data
  });
}

/**
 * 分配预约
 */
export function fetchAssignAppointment(id: string, data: { providerId: string }) {
  return request({
    url: `/api/appointment/${id}/assign`,
    method: 'put',
    data
  });
}

/**
 * 取消预约
 */
export function fetchCancelAppointment(id: string, data: { reason: string }) {
  return request({
    url: `/api/appointment/${id}/cancel`,
    method: 'put',
    data
  });
}

/**
 * 作废预约
 */
export function fetchInvalidateAppointment(id: string, data: { reason: string }) {
  return request({
    url: `/api/appointment/${id}/invalidate`,
    method: 'put',
    data
  });
}

/**
 * 导入预约（Excel）
 */
export function fetchImportAppointment(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<{ successCount: number; failCount: number; errors: string[] }>({
    url: '/api/appointment/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

/**
 * 下载预约导入模板
 */
export function fetchDownloadAppointmentTemplate() {
  return request({
    url: '/api/appointment/template',
    method: 'get',
    responseType: 'blob'
  });
}

/**
 * 获取预约统计
 */
export function fetchGetAppointmentStatistics(params?: { areaId?: string; startDate?: string; endDate?: string }) {
  return request<Api.Appointment.Statistics>({
    url: '/api/appointment/statistics',
    method: 'get',
    params
  });
}

/**
 * 根据客户手机号获取预约历史
 */
export function fetchGetAppointmentsByPhone(phone: string) {
  return request<Api.Appointment.Appointment[]>({
    url: '/api/appointment/phone',
    method: 'get',
    params: { phone }
  });
}

/**
 * 获取预约时间轴
 */
export function fetchGetAppointmentTimeline(id: string) {
  return request<Api.Appointment.AppointmentTimeline>({
    url: `/api/appointment/${id}/timeline`,
    method: 'get'
  });
}

/**
 * 批量删除预约
 */
export function fetchBatchDeleteAppointment(ids: string[]) {
  return request({
    url: '/api/appointment/batch',
    method: 'post',
    data: ids
  });
}

/**
 * 编辑预约业务信息（服务类型/预约时间/备注）
 */
export function fetchUpdateAppointment(id: string, data: Api.Appointment.AppointmentUpdateParams) {
  return request({
    url: `/api/appointment/${id}/update`,
    method: 'put',
    data
  });
}

// ========== 预约二维码 ==========

/**
 * 生成预约二维码（仅超级管理员）
 */
export function fetchGenerateAppointmentQRCode() {
  return request<{ token: string }>({
    url: '/api/appointment/qrcode/generate',
    method: 'post'
  });
}

/**
 * 获取二维码图片URL
 */
export function getQRCodeImageUrl(token: string) {
  return `/jxy/api/appointment/qrcode/${token}/image`;
}

/**
 * 停用二维码
 */
export function fetchDisableQRCode(token: string) {
  return request({
    url: `/api/appointment/qrcode/${token}/disable`,
    method: 'put'
  });
}
