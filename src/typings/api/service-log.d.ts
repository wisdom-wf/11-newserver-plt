/**
 * 服务日志模块（养老/家政共用）
 */

declare namespace Api {
  namespace ServiceLog {
    /** 服务日志查询参数 */
    interface ServiceLogQuery {
      /** 订单号 */
      orderNo?: string;
      /** 客户姓名 */
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
      /** 客户签字 */
      elderSign?: string;
      /** 服务人员签字 */
      staffSign?: string;
      /** 健康观察备注 */
      healthObservations?: string;
      /** 本次给药记录 */
      medicationGiven?: string;
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

    /** 服务日志详情 - 与后端ServiceLog实体对齐 */
    interface ServiceLog {
      /** 日志ID */
      serviceLogId: string;
      /** 日志编号 */
      logNo: string;
      /** 订单ID */
      orderId: string;
      /** 订单号 */
      orderNo?: string;
      /** 服务类别 */
      serviceCategory?: ServiceCategory;
      /** 客户ID */
      elderId: string;
      /** 客户姓名 */
      elderName: string;
      /** 客户手机号 */
      elderPhone?: string;
      /** 客户地址 */
      elderAddress?: string;
      /** 服务人员ID */
      staffId: string;
      /** 服务人员姓名 */
      staffName: string;
      /** 服务人员手机号 */
      staffPhone?: string;
      /** 服务商ID */
      providerId?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务类型名称 */
      serviceTypeName?: string;
      /** 服务日期 */
      serviceDate?: string;
      /** 服务开始时间 */
      serviceStartTime?: string;
      /** 服务结束时间 */
      serviceEndTime?: string;
      /** 服务时长（分钟） */
      serviceDuration?: number;
      /** 实际服务时长 */
      actualDuration?: number;
      /** 服务状态 - 与后端ServiceLogVO.status对齐 */
      status?: string;
      /** 服务状态 - 与后端serviceStatus对齐 */
      serviceStatus?: string;
      /** 服务评分 */
      serviceScore?: number;
      /** 服务评价 */
      serviceComment?: string;
      /** 服务照片 - 后端为逗号分隔字符串 */
      servicePhotos?: string | string[];
      /** 客户签名 - 与后端elderSignature对齐 */
      elderSignature?: string;
      /** 是否有异常 */
      hasAnomaly?: boolean;
      /** 异常类型 */
      anomalyType?: string;
      /** 异常描述 */
      anomalyDesc?: string;
      /** 异常照片 - 后端为逗号分隔字符串 */
      anomalyPhotos?: string | string[];
      /** 异常处理状态 */
      anomalyStatus?: string;
      /** 服务内容 */
      serviceContent?: string;
      /** 审核备注 */
      reviewRemarks?: string;
      /** 提交时间 */
      submitTime?: string;
      /** 审核状态：DRAFT-草稿, SUBMITTED-已提交, APPROVED-已通过, REJECTED-已驳回 */
      auditStatus?: string;
      /** 审核意见 */
      reviewComment?: string;
      /** 审核人ID */
      reviewerId?: string;
      /** 审核人姓名 */
      reviewerName?: string;
      /** 审核时间 */
      reviewTime?: string;
      /** 健康观察备注 */
      healthObservations?: string;
      /** 本次给药记录 */
      medicationGiven?: string;
      /** 服务照片列表 */
      servicePhotoList?: string[];
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 服务日志状态 */
    type LogStatus = 'DRAFT' | 'SUBMITTED' | 'VERIFIED';

    /** 服务日志统计 */
    interface Statistics {
      // 核心数量
      /** 总服务日志数 */
      total: number;
      /** 今日新增 */
      today: number;
      /** 本月新增 */
      month: number;

      // 审核状态
      /** 待审核数 */
      pendingCount: number;
      /** 已通过数 */
      approvedCount: number;
      /** 已驳回数 */
      rejectedCount: number;
      /** 审核通过率(%) */
      approvalRate: number;
      /** 待审核率(%) */
      pendingRate: number;

      // 服务质量
      /** 平均服务时长(分钟) */
      avgDuration: number;
      /** 平均服务评分 */
      avgScore: number;
      /** 异常服务次数 */
      anomalyCount: number;
      /** 异常率(%) */
      anomalyRate: number;

      // 审核效率
      /** 平均审核耗时(小时) */
      avgReviewTime: number;

      // 服务人员排名
      /** 服务人员排名列表 */
      staffRankings?: StaffRanking[];
    }

    /** 服务人员排名 */
    interface StaffRanking {
      staffId: string;
      staffName: string;
      providerName: string;
      logCount: number;
      approvedCount: number;
      rejectedCount: number;
      approvalRate: number;
    }
  }
}
