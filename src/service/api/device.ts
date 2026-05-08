import { request } from '../request';

/** 获取设备列表 */
export function fetchGetDeviceList(params?: Api.Device.DeviceQuery) {
  return request<Api.Common.PaginatingQueryRecord<Api.Device.Device>>({
    url: '/api/devices',
    method: 'get',
    params
  });
}

/** 根据序列号查询设备 */
export function fetchGetDeviceBySn(sn: string) {
  return request<Api.Device.Device>({
    url: `/api/devices/by-sn/${sn}`,
    method: 'get'
  });
}

/** 绑定设备到客户 */
export function fetchBindDevice(data: Api.Device.BindRequest) {
  return request<Api.Device.Binding>({
    url: '/api/devices/bind',
    method: 'post',
    data
  });
}

/** 解绑设备 */
export function fetchUnbindDevice(bindingId: string) {
  return request<void>({
    url: `/api/devices/bindings/${bindingId}`,
    method: 'delete'
  });
}

/** 获取客户的设备绑定列表 */
export function fetchGetElderDevices(elderId: string) {
  return request<Api.Device.Binding[]>({
    url: `/api/devices/elder/${elderId}`,
    method: 'get'
  });
}

/** 获取绑定列表 */
export function fetchGetBindingList(params?: Api.Device.DeviceQuery) {
  return request<Api.Common.PaginatingQueryRecord<Api.Device.Binding>>({
    url: '/api/devices/bindings',
    method: 'get',
    params
  });
}
