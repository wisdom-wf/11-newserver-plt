/**
 * 预约管理模块
 */

declare namespace Api {
  namespace Appointment {
    /** 预约查询参数 */
    interface AppointmentQuery {
      /** 预约单号 */
      appointmentNo?: string;
      /** 老人姓名 */
      elderName?: string;
      /** 老人手机号 */
      elderPhone?: string;
      /** 服务类型 */
      serviceType?: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 预约状态 */
      status?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 预约详情 */
    interface Appointment {
      /** 预约ID */
      appointmentId: string;
      /** 预约单号 */
      appointmentNo: string;
      /** 老人姓名 */
      elderName: string;
      /** 老人身份证号 */
      elderIdCard: string;
      /** 老人手机号 */
      elderPhone: string;
      /** 老人地址 */
      elderAddress: string;
      /** 区域ID */
      elderAreaId?: string;
      /** 区域名称 */
      elderAreaName?: string;
      /** 预约服务类型 */
      serviceType: string;
      /** 服务类型编码 */
      serviceTypeCode: string;
      /** 服务内容类型 */
      serviceContent?: string;
      /** 预约时间 */
      appointmentTime: string;
      /** 预计服务时长（分钟） */
      serviceDuration?: number;
      /** 服务机构ID */
      providerId?: string;
      /** 服务机构名称 */
      providerName?: string;
      /** 服务机构地址 */
      providerAddress?: string;
      /** 来访人数 */
      visitorCount?: number;
      /** 备注 */
      remark?: string;
      /** 预约状态 */
      status: AppointmentStatus;
      /** 数据有效性 */
      validity: Validity;
      /** 作废原因 */
      cancelReason?: string;
      /** 回复信息 */
      replyInfo?: string;
      /** 评估类型 */
      assessmentType?: string;
      /** 创建时间 */
      createTime: string;
      /** 确认时间 */
      confirmTime?: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 预约状态 */
    type AppointmentStatus =
      | 'PENDING'
      | 'CONFIRMED'
      | 'ASSIGNED'
      | 'IN_SERVICE'
      | 'COMPLETED'
      | 'CANCELLED'
      | 'INVALID';

    /** 数据有效性 */
    type Validity = 'VALID' | 'INVALID';

    /** 预约统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 待处理数 */
      pending: number;
      /** 已确认数 */
      confirmed: number;
      /** 已分配数 */
      assigned: number;
      /** 已完成数 */
      completed: number;
      /** 已取消数 */
      cancelled: number;
      /** 已作废数 */
      invalid: number;
    }

    /** 预约时间轴 */
    interface AppointmentTimeline {
      appointmentId: string;
      appointmentNo: string;
      currentStatus: string;
      currentStatusName: string;
      /** 关联的订单ID */
      orderId?: string;
      /** 关联的订单号 */
      orderNo?: string;
      /** 订单状态 */
      orderStatus?: string;
      /** 订单状态名称 */
      orderStatusName?: string;
      nodes: AppointmentTimelineNode[];
    }

    /** 预约时间轴节点 */
    interface AppointmentTimelineNode {
      status: string;
      statusName: string;
      title: string;
      time: string;
      details: AppointmentTimelineDetail[];
      completed: boolean;
      active: boolean;
    }

    /** 预约时间轴详情项 */
    interface AppointmentTimelineDetail {
      label: string;
      value: string;
    }
  }
}
