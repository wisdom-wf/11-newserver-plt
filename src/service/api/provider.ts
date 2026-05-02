import { request } from '../request';

/**
 * 获取服务商列表
 */
export function fetchGetProviderList(params?: Api.Provider.ProviderQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Provider.Provider>>({
    url: '/api/providers',
    method: 'get',
    params
  });
}

/**
 * 获取服务商详情
 */
export function fetchGetProvider(id: string) {
  return request<Api.Provider.Provider>({
    url: `/api/providers/${id}`,
    method: 'get'
  });
}

/**
 * 创建服务商
 */
export function fetchCreateProvider(data: Api.Provider.ProviderForm) {
  return request({
    url: '/api/providers',
    method: 'post',
    data
  });
}

/**
 * 更新服务商
 */
export function fetchUpdateProvider(id: string, data: Api.Provider.ProviderForm) {
  return request({
    url: `/api/providers/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除服务商
 */
export function fetchDeleteProvider(id: string) {
  return request({
    url: `/api/providers/${id}`,
    method: 'delete'
  });
}

/**
 * 更新服务商状态
 */
export function fetchUpdateProviderStatus(id: string, status: Api.Common.EnableStatus) {
  return request({
    url: `/api/providers/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 获取服务商统计
 */
export function fetchGetProviderStatistics(params?: { areaId?: string }) {
  return request<Api.Provider.Statistics>({
    url: '/api/statistics/provider',
    method: 'get',
    params
  });
}

/**
 * 获取服务商选择列表（用于下拉选择）
 * 从 /api/providers 接口提取 id 和 name
 */
export async function fetchGetProviderOptions(params?: { areaId?: string; serviceCategory?: string }) {
  const response = await request<Api.Common.PaginatingQueryRecord<Api.Provider.Provider>>({
    url: '/api/providers',
    method: 'get',
    params: {
      ...params,
      pageSize: 1000 // 获取全部，用于下拉选择
    }
  });

  // 提取为 options 格式
  if (response.data && response.data.records) {
    return response.data.records.map((item: Api.Provider.Provider) => ({
      id: item.providerId,
      name: item.providerName,
      status: item.status
    }));
  }
  return [];
}

/**
 * 创建服务商资质证书
 */
export function fetchCreateProviderCertificate(providerId: string, data: {
  qualificationName: string;
  qualificationType: string;
  qualificationNumber?: string;
  issueDate?: string;
  expiryDate?: string;
  issueOrganization?: string;
  attachmentUrl: string;
}) {
  return request({
    url: `/api/providers/${providerId}/certificates`,
    method: 'post',
    data
  });
}

/**
 * 删除服务商资质证书
 */
export function fetchDeleteProviderCertificate(qualificationId: string) {
  return request({
    url: `/api/providers/certificates/${qualificationId}`,
    method: 'delete'
  });
}

/**
 * 获取服务商资质证书列表
 */
export function fetchGetProviderCertificates(providerId: string) {
  return request<Api.Provider.Qualification[]>({
    url: `/api/providers/${providerId}/certificates`,
    method: 'get'
  });
}

/**
 * 获取服务商管理员账户
 */
export function fetchGetProviderAdminAccount(providerId: string) {
  return request<{ userId: string; username: string; status: string; createTime: string } | null>({
    url: `/api/providers/${providerId}/admin-account`,
    method: 'get'
  });
}

/**
 * 批量删除服务商
 */
export function fetchBatchDeleteProvider(ids: string[]) {
  return request({
    url: '/api/providers/batch',
    method: 'post',
    data: ids
  });
}

/**
 * 重置服务商管理员密码
 */
export function fetchResetProviderAdminPassword(providerId: string) {
  return request<string>({
    url: `/api/providers/${providerId}/admin-account/reset`,
    method: 'post'
  });
}
