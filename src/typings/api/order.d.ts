/**
 * 订单管理模块
 */

declare namespace Api {
  namespace Order {
    /** 订单查询参数 */
    interface OrderQuery {
      /** 订单号 */
      orderNo?: string;
      /** 老人姓名 */
      elderName?: string;
      /** 服务类型 */
      serviceType?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 订单状态 */
      status?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 服务人员ID */
      staffId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 订单表单 */
    interface OrderForm {
      /** 订单ID（编辑时需要） */
      id?: string;
      /** 预约ID */
      appointmentId?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 老人ID */
      elderId: string;
      /** 老人姓名 */
      elderName: string;
      /** 老人手机号 */
      elderPhone?: string;
      /** 老人地址 */
      elderAddress?: string;
      /** 区域ID */
      elderAreaId?: string;
      /** 服务类型 */
      serviceType: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务内容 */
      serviceContent?: string;
      /** 预约服务时间 */
      serviceTime: string;
      /** 服务时长 */
      serviceDuration?: number;
      /** 服务商ID */
      providerId: string;
      /** 服务人员ID */
      staffId?: string;
      /** 备注 */
      remark?: string;
    }

    /** 订单详情 */
    interface Order {
      /** 订单ID */
      id: string;
      /** 订单号 */
      orderNo: string;
      /** 预约ID */
      appointmentId?: string;
      /** 预约单号 */
      appointmentNo?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 老人ID */
      elderId: string;
      /** 老人姓名 */
      elderName: string;
      /** 老人手机号 */
      elderPhone?: string;
      /** 老人地址 */
      elderAddress?: string;
      /** 区域ID */
      elderAreaId?: string;
      /** 区域名称 */
      elderAreaName?: string;
      /** 服务类型 */
      serviceType: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务内容 */
      serviceContent?: string;
      /** 预约服务时间 */
      serviceTime: string;
      /** 服务时长（分钟） */
      serviceDuration?: number;
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName?: string;
      /** 服务人员ID */
      staffId?: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 服务人员手机号 */
      staffPhone?: string;
      /** 服务费 */
      serviceFee?: number;
      /** 补贴金额 */
      subsidyFee?: number;
      /** 自付金额 */
      selfPayFee?: number;
      /** 实际收费 */
      actualFee?: number;
      /** 订单状态 */
      status: OrderStatus;
      /** 备注 */
      remark?: string;
      /** 创建时间 */
      createTime: string;
      /** 分配时间 */
      assignTime?: string;
      /** 开始服务时间 */
      startTime?: string;
      /** 完成时间 */
      completeTime?: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 服务类别 */
    type ServiceCategory = 'ELDER_CARE' | 'HOME_CARE';

    /** 订单状态 */
    type OrderStatus = 'PENDING' | 'ASSIGNED' | 'ACCEPTED' | 'IN_SERVICE' | 'COMPLETED' | 'CANCELLED';

    /** 订单统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 今日订单 */
      today: number;
      /** 本月订单 */
      month: number;
      /** 待分配 */
      pending: number;
      /** 已分配 */
      assigned: number;
      /** 服务中 */
      inService: number;
      /** 已完成 */
      completed: number;
      /** 已取消 */
      cancelled: number;
      /** 完成率 */
      completionRate: number;
      /** 平均服务时长 */
      avgDuration: number;
    }
  }
}
