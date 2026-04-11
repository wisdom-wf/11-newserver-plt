/**
 * 服务质检模块
 */

declare namespace Api {
  namespace Quality {
    /** 质检查询参数 */
    interface QualityCheckQuery {
      /** 订单号 */
      orderNo?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 质检类型 */
      checkType?: string;
      /** 质检结果 */
      checkResult?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 质检表单 */
    interface QualityCheckForm {
      /** 质检ID（编辑时需要） */
      id?: string;
      /** 订单ID */
      orderId: string;
      /** 质检类型 */
      checkType: CheckType;
      /** 质检方式 */
      checkMethod: CheckMethod;
      /** 质检评分 */
      checkScore: number;
      /** 质检结果 */
      checkResult: CheckResult;
      /** 质检照片 */
      checkPhotos?: string[];
      /** 质检备注 */
      checkRemark?: string;
      /** 是否需要整改 */
      needRectify?: boolean;
      /** 整改通知 */
      rectifyNotice?: string;
      /** 整改期限 */
      rectifyDeadline?: string;
    }

    /** 质检详情 */
    interface QualityCheck {
      /** 质检ID */
      id: string;
      /** 质检编号 */
      checkNo: string;
      /** 订单ID */
      orderId: string;
      /** 订单号 */
      orderNo?: string;
      /** 服务日志ID */
      serviceLogId?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName?: string;
      /** 服务人员ID */
      staffId: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 质检类型 */
      checkType: CheckType;
      /** 质检方式 */
      checkMethod: CheckMethod;
      /** 综合评分 */
      checkScore: number;
      /** 质检结果 */
      checkResult: CheckResult;
      /** 质检照片 */
      checkPhotos?: string[];
      /** 质检备注 */
      checkRemark?: string;
      /** 质检时间 */
      checkTime?: string;
      /** 质检员ID */
      checkerId?: string;
      /** 质检员姓名 */
      checkerName?: string;
      /** 是否需要整改 */
      needRectify?: boolean;
      /** 整改通知 */
      rectifyNotice?: string;
      /** 整改期限 */
      rectifyDeadline?: string;
      /** 整改状态 */
      rectifyStatus?: RectifyStatus;
      /** 整改照片 */
      rectifyPhotos?: string[];
      /** 整改说明 */
      rectifyRemark?: string;
      /** 复检时间 */
      recheckTime?: string;
      /** 复检结果 */
      recheckResult?: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 质检类型 */
    type CheckType = 'RANDOM' | 'SCHEDULED' | 'COMPLAINT' | 'COMPLETION';

    /** 质检方式 */
    type CheckMethod = 'PHOTO_REVIEW' | 'PHONE_REVIEW' | 'ON_SITE';

    /** 质检结果 */
    type CheckResult = 'QUALIFIED' | 'UNQUALIFIED' | 'NEED_RECTIFY';

    /** 整改状态 */
    type RectifyStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'VERIFIED' | 'FAILED';

    /** 质检统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 合格数 */
      qualifiedCount: number;
      /** 不合格数 */
      unqualifiedCount: number;
      /** 需整改数 */
      needRectifyCount: number;
      /** 合格率 */
      qualifiedRate: number;
      /** 平均评分 */
      avgScore: number;
      /** 好评数 */
      positiveCount?: number;
      /** 中评数 */
      neutralCount?: number;
      /** 差评数 */
      negativeCount?: number;
      /** 投诉类型分布 */
      complaintTypes?: Array<{ typeName?: string; complaintType?: string; count?: number }>;
    }

    /** 养老服务评分明细 */
    interface ElderCareScore {
      /** 服务态度（20分） */
      serviceAttitude: number;
      /** 准时性（20分） */
      punctuality: number;
      /** 服务质量（40分） */
      serviceQuality: number;
      /** 老人满意度（20分） */
      elderSatisfaction: number;
      /** 总分 */
      totalScore: number;
    }

    /** 家政服务评分明细 */
    interface HomeCareScore {
      /** 清洁度（30分） */
      cleaningDegree: number;
      /** 规范性（25分） */
      specification: number;
      /** 物品保护（20分） */
      itemProtection: number;
      /** 客户满意度（25分） */
      customerSatisfaction: number;
      /** 总分 */
      totalScore: number;
    }
  }
}
