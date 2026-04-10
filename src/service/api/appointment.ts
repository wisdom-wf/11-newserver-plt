import { request } from '../request';

/**
 * 获取预约列表
 */
export function fetchGetAppointmentList(params?: Api.Appointment.AppointmentQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Appointment.Appointment>>({
    url: '/appointment/list',
    method: 'get',
    params
  });
}

/**
 * 获取预约详情
 */
export function fetchGetAppointment(id: string) {
  return request<Api.Appointment.Appointment>({
    url: `/appointment/${id}`,
    method: 'get'
  });
}

/**
 * 确认预约
 */
export function fetchConfirmAppointment(id: string, data: { providerId: string; appointmentTime: string }) {
  return request({
    url: `/appointment/${id}/confirm`,
    method: 'put',
    data
  });
}

/**
 * 分配预约
 */
export function fetchAssignAppointment(id: string, data: { providerId: string }) {
  return request({
    url: `/appointment/${id}/assign`,
    method: 'put',
    data
  });
}

/**
 * 取消预约
 */
export function fetchCancelAppointment(id: string, data: { reason: string }) {
  return request({
    url: `/appointment/${id}/cancel`,
    method: 'put',
    data
  });
}

/**
 * 作废预约
 */
export function fetchInvalidateAppointment(id: string, data: { reason: string }) {
  return request({
    url: `/appointment/${id}/invalidate`,
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
    url: '/appointment/import',
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
    url: '/appointment/template',
    method: 'get',
    responseType: 'blob'
  });
}

/**
 * 获取预约统计
 */
export function fetchGetAppointmentStatistics(params?: { areaId?: string; startDate?: string; endDate?: string }) {
  return request<Api.Appointment.Statistics>({
    url: '/appointment/statistics',
    method: 'get',
    params
  });
}

/**
 * 根据老人手机号获取预约历史
 */
export function fetchGetAppointmentsByPhone(phone: string) {
  return request<Api.Appointment.Appointment[]>({
    url: '/appointment/phone',
    method: 'get',
    params: { phone }
  });
}
