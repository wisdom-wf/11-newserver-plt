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
    }

    /** 评价详情 */
    interface Evaluation {
      /** 评价ID */
      id: string;
      /** 评价编号 */
      evalNo: string;
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
      /** 评价人ID */
      elderId: string;
      /** 评价人姓名 */
      elderName: string;
      /** 评价人手机号 */
      elderPhone?: string;
      /** 服务评分 */
      serviceScore: number;
      /** 态度评分 */
      attitudeScore: number;
      /** 技能评分 */
      skillScore: number;
      /** 准时评分 */
      punctualityScore: number;
      /** 综合评分 */
      overallScore: number;
      /** 满意度等级 */
      satisfactionLevel: SatisfactionLevel;
      /** 评价内容 */
      content?: string;
      /** 评价图片 */
      images?: string[];
      /** 机构回复 */
      reply?: string;
      /** 回复时间 */
      replyTime?: string;
      /** 创建时间 */
      createTime: string;
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
  }
}
