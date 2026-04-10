import { request } from '../request';

/**
 * 获取服务人员列表
 */
export function fetchGetStaffList(params?: Api.Staff.StaffQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Staff.Staff>>({
    url: '/staff/list',
    method: 'get',
    params
  });
}

/**
 * 获取服务人员详情
 */
export function fetchGetStaff(id: string) {
  return request<Api.Staff.Staff>({
    url: `/staff/${id}`,
    method: 'get'
  });
}

/**
 * 创建服务人员
 */
export function fetchCreateStaff(data: Api.Staff.StaffForm) {
  return request({
    url: '/staff',
    method: 'post',
    data
  });
}

/**
 * 更新服务人员
 */
export function fetchUpdateStaff(id: string, data: Api.Staff.StaffForm) {
  return request({
    url: `/staff/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除服务人员
 */
export function fetchDeleteStaff(id: string) {
  return request({
    url: `/staff/${id}`,
    method: 'delete'
  });
}

/**
 * 更新服务人员状态
 */
export function fetchUpdateStaffStatus(id: string, status: Api.Common.EnableStatus) {
  return request({
    url: `/staff/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 获取服务人员统计
 */
export function fetchGetStaffStatistics(params?: { providerId?: string; areaId?: string }) {
  return request<Api.Staff.Statistics>({
    url: '/staff/statistics',
    method: 'get',
    params
  });
}

/**
 * 获取服务人员选择列表
 */
export function fetchGetStaffOptions(params?: { providerId?: string; serviceCategory?: string; serviceType?: string }) {
  return request<{ id: string; name: string; phone?: string }[]>({
    url: '/staff/options',
    method: 'get',
    params
  });
}
