/**
 * 服务日志模块（养老/家政共用）
 */

declare namespace Api {
  namespace ServiceLog {
    /** 服务日志查询参数 */
    interface ServiceLogQuery {
      /** 订单号 */
      orderNo?: string;
      /** 老人姓名 */
      elderName?: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 服务类型 */
      serviceType?: string;
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

    /** 服务日志表单 */
    interface ServiceLogForm {
      /** 日志ID（编辑时需要） */
      id?: string;
      /** 订单ID */
      orderId: string;
      /** 服务开始时间 */
      serviceStartTime: string;
      /** 服务结束时间 */
      serviceEndTime: string;
      /** 实际服务时长（分钟） */
      serviceDuration?: number;
      /** 服务内容描述 */
      serviceContent?: string;
      /** 已完成项目 */
      completedItems?: string[];
      /** 服务照片 */
      servicePhotos?: string[];
      /** 是否有异常 */
      hasAnomaly?: boolean;
      /** 异常类型 */
      anomalyType?: string;
      /** 异常描述 */
      anomalyDesc?: string;
      /** 异常照片 */
      anomalyPhotos?: string[];
      /** 老人签字 */
      elderSign?: string;
      /** 服务人员签字 */
      staffSign?: string;
      /** 养老服务扩展字段 */
      elderCareExtend?: ElderCareExtend;
      /** 家政服务扩展字段 */
      homeCareExtend?: HomeCareExtend;
    }

    /** 养老服务扩展字段 */
    interface ElderCareExtend {
      /** 生命体征 */
      vitalSigns?: {
        bloodPressure?: string;
        heartRate?: number;
        temperature?: number;
        bloodSugar?: number;
      };
      /** 用药记录 */
      medicationRecord?: string;
      /** 饮食记录 */
      dietRecord?: string;
      /** 情绪状态 */
      moodState?: string;
      /** 特殊护理 */
      specialCare?: string[];
    }

    /** 家政服务扩展字段 */
    interface HomeCareExtend {
      /** 清洁度评分 */
      cleaningDegree?: number;
      /** 清洁区域 */
      cleaningAreas?: string[];
      /** 清洁项目 */
      cleaningItems?: string[];
      /** 使用物料 */
      materialsUsed?: {
        name: string;
        quantity: number;
        brand?: string;
      }[];
    }

    /** 服务日志详情 */
    interface ServiceLog {
      /** 日志ID */
      id: string;
      /** 日志编号 */
      logNo: string;
      /** 订单ID */
      orderId: string;
      /** 订单号 */
      orderNo?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 老人ID */
      elderId: string;
      /** 老人姓名 */
      elderName: string;
      /** 老人手机号 */
      elderPhone?: string;
      /** 服务人员ID */
      staffId: string;
      /** 服务人员姓名 */
      staffName: string;
      /** 服务人员手机号 */
      staffPhone?: string;
      /** 服务类型 */
      serviceType: string;
      /** 服务开始时间 */
      serviceStartTime?: string;
      /** 服务结束时间 */
      serviceEndTime?: string;
      /** 实际服务时长（分钟） */
      serviceDuration?: number;
      /** 服务内容描述 */
      serviceContent?: string;
      /** 已完成项目 */
      completedItems?: string[];
      /** 服务照片 */
      servicePhotos?: string[];
      /** 是否有异常 */
      hasAnomaly?: boolean;
      /** 异常类型 */
      anomalyType?: string;
      /** 异常描述 */
      anomalyDesc?: string;
      /** 老人签字 */
      elderSign?: string;
      /** 服务人员签字 */
      staffSign?: string;
      /** 养老服务扩展 */
      elderCareExtend?: ElderCareExtend;
      /** 家政服务扩展 */
      homeCareExtend?: HomeCareExtend;
      /** 状态 */
      status: LogStatus;
      /** 提交时间 */
      submitTime?: string;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 服务日志状态 */
    type LogStatus = 'DRAFT' | 'SUBMITTED' | 'VERIFIED';

    /** 服务日志统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 今日服务 */
      today: number;
      /** 本月服务 */
      month: number;
      /** 平均服务时长 */
      avgDuration: number;
      /** 异常数 */
      anomalyCount: number;
    }
  }
}
