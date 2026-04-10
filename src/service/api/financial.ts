import { request } from '../request';

/**
 * 获取结算列表
 */
export function fetchGetSettlementList(params?: Api.Financial.SettlementQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Financial.Settlement>>({
    url: '/financial/settlement/list',
    method: 'get',
    params
  });
}

/**
 * 获取结算详情
 */
export function fetchGetSettlement(id: string) {
  return request<Api.Financial.Settlement>({
    url: `/financial/settlement/${id}`,
    method: 'get'
  });
}

/**
 * 创建结算
 */
export function fetchCreateSettlement(data: Api.Financial.SettlementForm) {
  return request({
    url: '/financial/settlement',
    method: 'post',
    data
  });
}

/**
 * 确认结算
 */
export function fetchConfirmSettlement(id: string) {
  return request({
    url: `/financial/settlement/${id}/confirm`,
    method: 'put'
  });
}

/**
 * 取消结算
 */
export function fetchCancelSettlement(id: string, reason?: string) {
  return request({
    url: `/financial/settlement/${id}/cancel`,
    method: 'put',
    data: { reason }
  });
}

/**
 * 获取结算统计
 */
export function fetchGetSettlementStatistics(params?: {
  areaId?: string;
  providerId?: string;
  startDate?: string;
  endDate?: string;
}) {
  return request<Api.Financial.Statistics>({
    url: '/financial/settlement/statistics',
    method: 'get',
    params
  });
}

/**
 * 获取发票列表
 */
export function fetchGetInvoiceList(params?: Api.Financial.InvoiceQuery & Api.Common.PaginatingQueryParams) {
  return request<Api.Common.PaginatingQueryRecord<Api.Financial.Invoice>>({
    url: '/financial/invoice/list',
    method: 'get',
    params
  });
}

/**
 * 获取发票详情
 */
export function fetchGetInvoice(id: string) {
  return request<Api.Financial.Invoice>({
    url: `/financial/invoice/${id}`,
    method: 'get'
  });
}

/**
 * 开具发票
 */
export function fetchCreateInvoice(data: {
  orderId: string;
  invoiceType: Api.Financial.InvoiceType;
  amount: number;
  remark?: string;
}) {
  return request({
    url: '/financial/invoice',
    method: 'post',
    data
  });
}

/**
 * 作废发票
 */
export function fetchVoidInvoice(id: string, reason: string) {
  return request({
    url: `/financial/invoice/${id}/void`,
    method: 'put',
    data: { reason }
  });
}
