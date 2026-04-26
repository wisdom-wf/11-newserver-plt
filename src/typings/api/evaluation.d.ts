/**
 * 满意度评价模块
 */

declare namespace Api {
  namespace Evaluation {
    /** 评价查询参数 */
    interface EvaluationQuery {
      /** 订单号 */
      orderNo?: string;
      /** 客户姓名 */
      elderName?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 满意度等级 */
      satisfactionLevel?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 评价表单 */
    interface EvaluationForm {
      /** 评价ID（编辑时需要） */
      id?: string;
      /** 订单ID */
      orderId: string;
      /** 服务评分（总体评分） */
      serviceScore: number;
      /** 态度评分 */
      attitudeScore: number;
      /** 技能评分 */
      skillScore: number;
      /** 准时评分 */
      punctualityScore: number;
      /** 环境评分 */
      environmentScore: number;
      /** 综合评分 */
      overallScore: number;
      /** 评价内容 */
      content?: string;
      /** 评价图片 */
      images?: string[];
      /** 关联质检单ID（从质检详情跳转时带入） */
      qualityCheckId?: string;
      /** 关联服务日志ID */
      serviceLogId?: string;
    }

    /** 评价详情 */
    interface Evaluation {
      /** 评价ID */
      evaluationId: string;
      /** 订单ID */
      orderId: string;
      /** 关联质检单ID */
      qualityCheckId?: string;
      /** 关联服务日志ID */
      serviceLogId?: string;
      /** 服务人员ID */
      staffId: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName?: string;
      /** 评价人ID */
      elderId: string;
      /** 评价人姓名 */
      elderName: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务类型名称 */
      serviceTypeName?: string;
      /** 态度评分 */
      attitudeScore?: number;
      /** 质量评分 */
      qualityScore?: number;
      /** 效率评分 */
      efficiencyScore?: number;
      /** 环境评分 */
      environmentScore?: number;
      /** 综合评分 */
      overallScore?: number;
      /** 平均评分 */
      averageScore?: number;
      /** 评价内容 */
      content?: string;
      /** 评价标签 */
      tags?: string;
      /** 是否匿名 */
      anonymous?: number;
      /** 评价时间 */
      evaluationTime?: string;
      /** 回复内容 */
      replyContent?: string;
      /** 回复时间 */
      replyTime?: string;
      /** 机构回复 */
      reply?: string;
      /** 回复人ID */
      replyerId?: string;
      /** 回复人姓名 */
      replyerName?: string;
      /** 创建时间 */
      createTime?: string;
    }

    /** 满意度等级 */
    type SatisfactionLevel = 'VERY_SATISFIED' | 'SATISFIED' | 'NEUTRAL' | 'DISSATISFIED' | 'VERY_DISSATISFIED';

    /** 评价统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 平均综合评分 */
      avgOverallScore: number;
      /** 平均服务评分 */
      avgServiceScore: number;
      /** 平均态度评分 */
      avgAttitudeScore: number;
      /** 平均技能评分 */
      avgSkillScore: number;
      /** 平均准时评分 */
      avgPunctualityScore: number;
      /** 非常满意数 */
      verySatisfiedCount: number;
      /** 满意数 */
      satisfiedCount: number;
      /** 一般数 */
      neutralCount: number;
      /** 不满意数 */
      dissatisfiedCount: number;
      /** 非常不满意数 */
      veryDissatisfiedCount: number;
      /** 满意率 */
      satisfactionRate: number;
    }

    /** 评价邀请信息 */
    interface EvaluationInvite {
      /** 评价ID */
      evaluationId: string;
      /** 评价邀请Token */
      token: string;
      /** 评价邀请链接 */
      surveyUrl: string;
      /** 订单ID */
      orderId: string;
      /** 老人ID */
      elderId: string;
      /** 老人姓名 */
      elderName: string;
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName: string;
      /** 服务人员ID */
      staffId: string;
      /** 服务人员姓名 */
      staffName: string;
      /** 服务类型 */
      serviceType: string;
      /** Token状态 */
      tokenStatus: 'PENDING' | 'COMPLETED' | 'EXPIRED' | 'INVALID';
      /** Token过期时间 */
      tokenExpireTime: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 问卷表单提交 */
    interface SurveyForm {
      /** 服务评分(1-5) */
      serviceScore: number;
      /** 态度评分(1-5) */
      attitudeScore: number;
      /** 技能评分(1-5) */
      skillScore: number;
      /** 准时评分(1-5) */
      punctualityScore: number;
      /** 环境评分(1-5) */
      environmentScore?: number;
      /** 满意度等级 */
      satisfactionLevel?: string;
      /** 评价内容 */
      content?: string;
      /** 评价标签 */
      tags?: string[];
      /** 评价图片 */
      images?: string[];
      /** 是否匿名 */
      anonymous?: boolean;
    }
  }
}
