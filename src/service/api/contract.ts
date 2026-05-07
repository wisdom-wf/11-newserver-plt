import { request } from '../request';

/**
 * 获取订单关联的合同
 */
export function fetchGetContractByOrderId(orderId: string) {
  return request<Api.Ess.Contract>({
    url: `/api/contracts/order/${orderId}`,
    method: 'get'
  });
}

/**
 * 获取签署链接
 */
export function fetchGetSignUrl(contractId: string, approverType: number = 0) {
  return request<Api.Ess.SignUrl>({
    url: `/api/contracts/${contractId}/sign-url`,
    method: 'get',
    params: { approverType }
  });
}

/**
 * 检查合同状态
 */
export function fetchGetContractStatus(contractId: string) {
  return request<{ status: string }>({
    url: `/api/contracts/${contractId}/status`,
    method: 'get'
  });
}

/**
 * 下载合同
 */
export function fetchDownloadContract(contractId: string) {
  return request<{ downloadUrl: string }>({
    url: `/api/contracts/${contractId}/download`,
    method: 'get'
  });
}

/**
 * 获取合同列表（管理端）
 */
export function fetchGetContractList(params?: Api.Ess.ContractQuery) {
  return request<Api.Common.PaginatingQueryRecord<Api.Ess.Contract>>({
    url: '/api/contracts',
    method: 'get',
    params
  });
}