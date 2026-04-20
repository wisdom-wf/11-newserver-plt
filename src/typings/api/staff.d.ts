/**
 * 服务人员管理模块
 */

declare namespace Api {
  namespace Staff {
    /** 服务人员查询参数 */
    interface StaffQuery {
      /** 姓名 */
      staffName?: string;
      /** 工号 */
      staffNo?: string;
      /** 手机号 */
      phone?: string;
      /** 身份证号 */
      idCard?: string;
      /** 服务类型 */
      serviceType?: string;
      /** 服务商ID */
      providerId?: string;
      /** 区域ID */
      areaId?: string;
      /** 状态 */
      status?: string | number;
    }

    /** 服务人员表单 */
    interface StaffForm {
      /** 服务人员ID（编辑时需要） */
      staffId?: string;
      /** 姓名 */
      staffName: string;
      /** 性别 */
      gender?: number;
      /** 出生日期 */
      birthDate?: string;
      /** 身份证号 */
      idCard?: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      avatar?: string;
      /** 服务类型列表 */
      serviceTypes?: string;
      /** 所属服务商ID */
      providerId: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 简介 */
      remark?: string;
      /** 状态 */
      status?: string;
    }

    /** 服务人员详情 */
    interface Staff {
      /** 服务人员ID */
      staffId: string;
      /** 工号 */
      staffNo?: string;
      /** 姓名 */
      staffName: string;
      /** 性别：0-女，1-男 */
      gender?: number;
      /** 性别文本 */
      genderText?: string;
      /** 出生日期 */
      birthDate?: string;
      /** 年龄 */
      age?: number;
      /** 身份证号 */
      idCard?: string;
      /** 手机号 */
      phone?: string;
      /** 民族 */
      nation?: string;
      /** 学历 */
      education?: string;
      /** 学历文本 */
      educationText?: string;
      /** 政治面貌 */
      politicalStatus?: string;
      /** 政治面貌文本 */
      politicalStatusText?: string;
      /** 婚姻状况 */
      maritalStatus?: string;
      /** 婚姻状况文本 */
      maritalStatusText?: string;
      /** 户籍地址 */
      domicileAddress?: string;
      /** 居住地址 */
      residenceAddress?: string;
      /** 头像URL */
      avatarUrl?: string;
      /** 所属服务商ID */
      providerId: string;
      /** 所属服务商名称 */
      providerName?: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 服务类型 */
      serviceTypes?: string;
      /** 服务类型文本 */
      serviceTypesText?: string;
      /** 员工状态 */
      status?: string;
      /** 状态文本 */
      statusText?: string;
      /** 审核备注 */
      auditRemark?: string;
      /** 入职日期 */
      hireDate?: string;
      /** 离职日期 */
      leaveDate?: string;
      /** 离职原因 */
      leaveReason?: string;
      /** 备注 */
      remark?: string;
      /** 评分 */
      rating?: number;
      /** 接单数 */
      orderCount?: number;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 服务人员统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 在职 */
      active: number;
      /** 待上岗 */
      pending: number;
      /** 已离职 */
      inactive: number;
      /** 平均评分 */
      avgRating: number;
    }

    /** 服务日志（用于服务人员详情展示） */
    interface StaffServiceLog {
      /** 服务日志ID */
      serviceLogId: string;
      /** 订单ID */
      orderId?: string;
      /** 订单号 */
      orderNo?: string;
      /** 老人ID */
      elderId?: string;
      /** 老人姓名 */
      elderName?: string;
      /** 服务人员ID */
      staffId?: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 服务类型 */
      serviceType?: string;
      /** 服务类型名称 */
      serviceCategory?: string;
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
      /** 服务评价/内容 */
      serviceComment?: string;
      /** 服务内容 */
      serviceContent?: string;
      /** 服务状态 */
      status?: string;
      /** 提交时间 */
      submitTime?: string;
      /** 是否有异常 */
      hasAnomaly?: boolean;
      /** 异常类型 */
      anomalyType?: string;
      /** 异常描述 */
      anomalyDesc?: string;
      /** 异常处理状态 */
      anomalyStatus?: string;
      /** 创建时间 */
      createTime?: string;
      /** 服务照片 */
      servicePhotos?: string[];
      /** 审核状态 */
      auditStatus?: string;
      /** 审核意见 */
      reviewComment?: string;
      /** 审核人姓名 */
      reviewerName?: string;
      /** 审核时间 */
      reviewTime?: string;
      /** 健康观察备注 */
      healthObservations?: string;
      /** 本次给药记录 */
      medicationGiven?: string;
    }
  }
}
