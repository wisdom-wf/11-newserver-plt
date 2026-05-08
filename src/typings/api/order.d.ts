/**
 * 订单管理模块
 */

declare namespace Api {
  namespace Order {
    /** 订单查询参数 */
    interface OrderQuery {
      /** 订单号 */
      orderNo?: string;
      /** 客户姓名 */
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
      orderId?: string;
      /** 预约ID */
      appointmentId?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 客户ID */
      elderId: string;
      /** 客户姓名 */
      elderName: string;
      /** 客户手机号 */
      elderPhone?: string;
      /** 客户地址 */
      serviceAddress?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务类型 */
      serviceTypeName?: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务日期 */
      serviceDate?: string;
      /** 预约服务时间 */
      serviceTime?: string;
      /** 服务时长 */
      serviceDuration?: number;
      /** 服务商ID */
      providerId: string;
      /** 服务人员ID */
      staffId?: string;
      /** 备注 */
      remark?: string;
    }

    /** 订单状态枚举 - 与后端OrderStatus对齐 */
    type OrderStatus =
      | 'CANCELLED' // 0 - 已取消
      | 'CREATED' // 2 - 待派单
      | 'DISPATCHED' // 3 - 已派单
      | 'RECEIVED' // 4 - 已接单
      | 'SERVICE_STARTED' // 5 - 服务中
      | 'SERVICE_COMPLETED' // 6 - 已完成
      | 'EVALUATED' // 7 - 已评价
      | 'SETTLED' // 8 - 已结算
      | 'REJECTED'; // 9 - 已拒单

    /** 订单详情 */
    interface Order {
      /** 订单ID */
      orderId: string;
      /** 订单号 */
      orderNo: string;
      /** 预约ID */
      appointmentId?: string;
      /** 预约单号 */
      appointmentNo?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 客户ID */
      elderId: string;
      /** 客户姓名 */
      elderName: string;
      /** 客户手机号 */
      elderPhone?: string;
      /** 客户地址 */
      serviceAddress?: string;
      /** 区域ID */
      areaId?: string;
      /** 区域名称 */
      areaName?: string;
      /** 服务类型 */
      serviceTypeName?: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务日期 */
      serviceDate?: string;
      /** 预约服务时间 */
      serviceTime?: string;
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
      /** 订单类型 */
      orderType?: string;
      /** 订单来源 */
      orderSource?: string;
      /** 补贴类型 */
      subsidyType?: string;
      /** 预估价格 */
      estimatedPrice?: number;
      /** 实际价格 */
      actualPrice?: number;
      /** 补贴金额 */
      subsidyAmount?: number;
      /** 自付金额 */
      selfPayAmount?: number;
      /** 订单状态 - 字符串枚举 */
      status: OrderStatus;
      /** 状态数字编码 - 与前端历史兼容 */
      statusCode?: number;
      /** 状态名称 */
      statusName?: string;
      /** 取消原因 */
      cancelReason?: string;
      /** 派单时间 */
      dispatchTime?: string;
      /** 接单时间 */
      receiveTime?: string;
      /** 开始服务时间 */
      startTime?: string;
      /** 完成时间 */
      completeTime?: string;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 订单统计 */
    interface Statistics {
      // 核心数量
      /** 总订单数 */
      total: number;
      /** 今日新增 */
      today: number;
      /** 本月新增 */
      month: number;

      // 状态分布
      /** 待派单数 */
      pendingDispatch: number;
      /** 已派单数 */
      dispatched: number;
      /** 已接单数 */
      received: number;
      /** 服务中数 */
      inService: number;
      /** 已完成数 */
      completed: number;
      /** 已取消数 */
      cancelled: number;

      // 比率
      /** 完成率(%) */
      completionRate: number;
      /** 订单趋势 */
      orderTrend?: Array<{ date: string; orderCount: number; completedCount: number }>;
      /** 服务类型分布 */
      serviceTypeDistribution?: Array<{ serviceTypeName: string; orderCount: number }>;
      /** 取消率(%) */
      cancelRate: number;

      // 金额统计
      /** 总预估金额 */
      totalEstimatedPrice: number;
      /** 总实际金额 */
      totalActualPrice: number;
      /** 总补贴金额 */
      totalSubsidy: number;
      /** 总自付金额 */
      totalSelfPay: number;

      // 服务人员排名
      /** 服务人员排名列表 */
      staffRankings?: StaffRanking[];
    }

    /** 服务人员排名 */
    interface StaffRanking {
      staffId: string;
      staffName: string;
      providerName: string;
      orderCount: number;
      completedCount: number;
    }
  }
}
