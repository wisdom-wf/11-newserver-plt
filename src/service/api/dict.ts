import { request } from '../request';

/**
 * 获取字典类型列表
 */
export function fetchGetDictTypeList(
  params?: Api.Common.PaginatingQueryParams & { dictName?: string; dictCode?: string; status?: string }
) {
  return request<Api.Common.PaginatingQueryRecord<Api.System.DictType>>({
    url: '/system/dict/type/list',
    method: 'get',
    params
  });
}

/**
 * 获取字典类型详情
 */
export function fetchGetDictType(id: string) {
  return request<Api.System.DictType>({
    url: `/system/dict/type/${id}`,
    method: 'get'
  });
}

/**
 * 创建字典类型
 */
export function fetchCreateDictType(data: Api.System.DictTypeForm) {
  return request({
    url: '/system/dict/type',
    method: 'post',
    data
  });
}

/**
 * 更新字典类型
 */
export function fetchUpdateDictType(id: string, data: Api.System.DictTypeForm) {
  return request({
    url: `/system/dict/type/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除字典类型
 */
export function fetchDeleteDictType(id: string) {
  return request({
    url: `/system/dict/type/${id}`,
    method: 'delete'
  });
}

/**
 * 获取字典项列表
 */
export function fetchGetDictItemList(params: { dictTypeCode: string }) {
  return request<Api.System.DictItem[]>({
    url: '/system/dict/item/list',
    method: 'get',
    params
  });
}

/**
 * 获取字典项详情
 */
export function fetchGetDictItem(id: string) {
  return request<Api.System.DictItem>({
    url: `/system/dict/item/${id}`,
    method: 'get'
  });
}

/**
 * 创建字典项
 */
export function fetchCreateDictItem(data: Api.System.DictItemForm) {
  return request({
    url: '/system/dict/item',
    method: 'post',
    data
  });
}

/**
 * 更新字典项
 */
export function fetchUpdateDictItem(id: string, data: Api.System.DictItemForm) {
  return request({
    url: `/system/dict/item/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除字典项
 */
export function fetchDeleteDictItem(id: string) {
  return request({
    url: `/system/dict/item/${id}`,
    method: 'delete'
  });
}

/**
 * 根据字典编码获取字典项
 */
export function fetchGetDictItemsByCode(dictCode: string) {
  return request<Api.System.DictItem[]>({
    url: `/system/dict/items/${dictCode}`,
    method: 'get'
  });
}
