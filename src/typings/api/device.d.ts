/**
 * 设备管理模块
 */

declare namespace Api {
  namespace Device {
    /** 设备 */
    interface Device {
      deviceId: string;
      deviceSn: string;
      deviceType: string;
      deviceName?: string;
      manufacturer?: string;
      model?: string;
      status: string;
      statusText?: string;
      lastPushTime?: string;
      createTime?: string;
    }

    /** 设备绑定 */
    interface Binding {
      bindingId: string;
      deviceId: string;
      deviceSn?: string;
      deviceType?: string;
      deviceTypeName?: string;
      deviceName?: string;
      elderId: string;
      elderName?: string;
      measurementType: string;
      measurementTypeName?: string;
      bindTime: string;
      status: string;
      deviceStatus?: string;
      lastPushTime?: string;
      remark?: string;
    }

    /** 绑定请求 */
    interface BindRequest {
      deviceSn: string;
      elderId: string;
      measurementType: string;
      remark?: string;
    }

    /** 设备查询参数 */
    interface DeviceQuery {
      deviceType?: string;
      status?: string;
      elderId?: string;
      page?: number;
      pageSize?: number;
    }

    /** 设备类型 */
    type DeviceType = 'BP' | 'BG' | 'WT' | 'TP' | 'PL' | 'SP';

    /** 测量类型与设备类型映射 */
    const MEASUREMENT_TO_DEVICE: Record<string, DeviceType>;
  }
}
