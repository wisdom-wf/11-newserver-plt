/**
 * 驾驶舱模块 - 管理员数据可视化大屏
 */

declare namespace Api {
  namespace Cockpit {
    /** 驾驶舱概览统计 */
    interface Overview {
      /** 今日订单 */
      todayOrders: number;
      /** 本月订单 */
      monthOrders: number;
      /** 累计订单 */
      totalOrders: number;
      /** 今日服务人次 */
      todayServices: number;
      /** 本月服务人次 */
      monthServices: number;
      /** 累计服务人次 */
      totalServices: number;
      /** 服务商数量 */
      providerCount: number;
      /** 服务人员数量 */
      staffCount: number;
      /** 老人数量 */
      elderCount: number;
      /** 本月营收 */
      monthRevenue: number;
      /** 累计营收 */
      totalRevenue: number;
      /** 满意度 */
      satisfaction: number;
      /** 合格率 */
      qualifiedRate: number;
    }

    /** 订单趋势 */
    interface OrderTrend {
      /** 日期 */
      date: string;
      /** 订单数 */
      orderCount: number;
      /** 服务人次 */
      serviceCount: number;
      /** 金额 */
      amount: number;
    }

    /** 服务类别分布 */
    interface ServiceDistribution {
      /** 服务类别 */
      category: ServiceCategory;
      /** 数量 */
      count: number;
      /** 占比 */
      proportion: number;
    }

    /** 区域分布 */
    interface AreaDistribution {
      /** 区域ID */
      areaId: string;
      /** 区域名称 */
      areaName: string;
      /** 订单数 */
      orderCount: number;
      /** 服务人次 */
      serviceCount: number;
      /** 金额 */
      amount: number;
      /** 占比 */
      proportion: number;
    }

    /** 服务商排行 */
    interface ProviderRanking {
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName: string;
      /** 订单数 */
      orderCount: number;
      /** 服务人次 */
      serviceCount: number;
      /** 评分 */
      rating: number;
      /** 金额 */
      amount: number;
    }

    /** 服务人员排行 */
    interface StaffRanking {
      /** 服务人员ID */
      staffId: string;
      /** 服务人员姓名 */
      staffName: string;
      /** 服务商名称 */
      providerName: string;
      /** 订单数 */
      orderCount: number;
      /** 服务人次 */
      serviceCount: number;
      /** 评分 */
      rating: number;
    }

    /** 满意度评价分布 */
    interface SatisfactionDistribution {
      /** 非常满意 */
      verySatisfied: number;
      /** 满意 */
      satisfied: number;
      /** 一般 */
      neutral: number;
      /** 不满意 */
      dissatisfied: number;
      /** 非常不满意 */
      veryDissatisfied: number;
    }

    /** 质检结果分布 */
    interface QualityDistribution {
      /** 合格 */
      qualified: number;
      /** 不合格 */
      unqualified: number;
      /** 需整改 */
      needRectify: number;
    }

    /** 财务收支趋势 */
    interface FinancialTrend {
      /** 日期 */
      date: string;
      /** 服务费 */
      serviceFee: number;
      /** 补贴金额 */
      subsidyFee: number;
      /** 自付金额 */
      selfPayFee: number;
      /** 合计 */
      total: number;
    }

    /** 年龄段分布 */
    interface AgeDistribution {
      /**年龄段 */
      ageRange: string;
      /** 数量 */
      count: number;
      /** 占比 */
      proportion: number;
    }

    /** 护理等级分布 */
    interface CareLevelDistribution {
      /** 护理等级 */
      careLevel: string;
      /** 数量 */
      count: number;
      /** 占比 */
      proportion: number;
    }

    /** 订单实时数据 */
    interface RealtimeOrder {
      /** 订单号 */
      orderNo: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 服务类型 */
      serviceType: string;
      /** 服务商名称 */
      providerName: string;
      /** 服务人员 */
      staffName: string;
      /** 老人姓名 */
      elderName: string;
      /** 状态 */
      status: OrderStatus;
      /** 时间 */
      time: string;
    }

    /** 预警信息 */
    interface Warning {
      /** 预警ID */
      id: string;
      /** 预警类型 */
      type: WarningType;
      /** 预警等级 */
      level: WarningLevel;
      /** 预警标题 */
      title: string;
      /** 预警内容 */
      content: string;
      /** 区域 */
      areaName?: string;
      /** 服务商 */
      providerName?: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 预警类型 */
    type WarningType = 'ORDER_DELAY' | 'QUALITY_ISSUE' | 'STAFF_ABSENT' | 'COMPLAINT' | 'FINANCIAL_EXCEPTION';

    /** 预警等级 */
    type WarningLevel = 'INFO' | 'WARNING' | 'CRITICAL';

    /** 驾驶舱查询参数 */
    interface CockpitQuery {
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }
  }
}
