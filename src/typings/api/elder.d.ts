/**
 * 客户档案管理模块
 */

declare namespace Api {
  namespace Elder {
    /** 性别 */
    type Gender = 'UNKNOWN' | 'MALE' | 'FEMALE' | '0' | '1';
    /** 养老类型 */
    type CareType = 'HOME' | 'COMMUNITY' | 'INSTITUTION';
    /** 护理等级 */
    type CareLevel = 'HIGH' | 'MEDIUM' | 'NORMAL';
    /** 补贴类型 */
    type SubsidyType = 'SELF_PAY' | 'SUBSIDY' | 'MIXED';
    /** 健康状况 */
    type HealthStatus = 'GOOD' | 'FAIR' | 'POOR' | 'UNKNOWN';

    /** 客户查询参数 */
    interface ElderQuery {
      /** 姓名 */
      name?: string;
      /** 身份证号 */
      idCard?: string;
      /** 手机号 */
      phone?: string;
      /** 区域ID */
      areaId?: string;
      /** 社区ID */
      communityId?: string;
      /** 养老类型 */
      careType?: string;
      /** 补贴类型 */
      subsidyType?: string;
      /** 状态 */
      status?: string;
    }

    /** 客户表单 */
    interface ElderForm {
      /** 客户ID（编辑时需要） */
      elderId?: string;
      /** 姓名 */
      name: string;
      /** 性别 */
      gender?: string;
      /** 出生日期 */
      birthDate?: string;
      /** 年龄 */
      age?: number;
      /** 身份证号 */
      idCard: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      photoUrl?: string;
      /** 区域ID */
      areaId?: string;
      /** 社区ID */
      communityId?: string;
      /** 详细地址 */
      address?: string;
      /** 养老类型 */
      careType?: string;
      /** 补贴类型 */
      subsidyType?: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 健康状况 */
      healthStatus?: string;
      /** 既往病史 */
      medicalHistory?: string;
      /** 过敏信息 */
      allergies?: string;
      /** 护理等级 */
      careLevel?: string;
      /** 民族 */
      ethnicity?: string;
      /** 婚姻状况 */
      maritalStatus?: string;
      /** 退休单位 */
      retiredFrom?: string;
      /** 养老金 */
      pension?: number;
      /** 备注 */
      remark?: string;
    }

    /** 客户详情 */
    interface Elder {
      /** 客户ID */
      elderId: string;
      /** 档案编号 */
      fileNo?: string;
      /** 姓名 - 与后端elder_name字段映射 */
      name: string;
      /** 客户姓名 - 兼容其他模块使用的elderName */
      elderName?: string;
      /** 性别 */
      gender?: string;
      /** 性别名称 */
      genderName?: string;
      /** 出生日期 */
      birthDate?: string;
      /** 年龄 */
      age?: number;
      /** 身份证号 */
      idCard: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      photoUrl?: string;
      /** 身份证照片 */
      idCardPhotos?: string[];
      /** 健康档案照片 */
      healthPhotos?: string[];
      /** 区域ID */
      areaId?: string;
      /** 区域名称 */
      areaName?: string;
      /** 社区ID */
      communityId?: string;
      /** 社区名称 */
      communityName?: string;
      /** 详细地址 */
      address?: string;
      /** 养老类型 */
      careType?: string;
      /** 补贴类型 */
      subsidyType?: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 健康状况 */
      healthStatus?: string;
      /** 既往病史 */
      medicalHistory?: string;
      /** 过敏信息 */
      allergies?: string;
      /** 护理等级 */
      careLevel?: string;
      /** 护理等级名称 */
      careLevelName?: string;
      /** 民族 */
      ethnicity?: string;
      /** 婚姻状况 */
      maritalStatus?: string;
      /** 退休单位 */
      retiredFrom?: string;
      /** 养老金 */
      pension?: number;
      /** 备注 */
      remark?: string;
      /** 状态 */
      status?: string;
      /** 状态名称 */
      statusName?: string;
      /** 关联服务商ID */
      providerId?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 客户统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 在册 */
      registered: number;
      /** 暂停服务 */
      suspended: number;
      /** 养老类型分布 */
      careTypeStats: Record<string, number>;
      /** 护理等级分布 */
      careLevelStats: Record<string, number>;
      /** 补贴类型分布 */
      subsidyTypeStats: Record<string, number>;
      /** 年龄分布 */
      ageDistribution?: Array<{ ageRange: string; count: number }>;
      /** 服务需求分布 */
      serviceDemandDistribution?: Array<{ demandType: string; count: number }>;
    }

    /** 测量类型 */
    type MeasurementType = 'BLOOD_PRESSURE' | 'BLOOD_GLUCOSE' | 'WEIGHT' | 'TEMPERATURE' | 'PULSE' | 'SPO2' | 'PAIN_SCALE' | 'OTHER';

    /** 预警状态 */
    type AlertStatus = 'NORMAL' | 'WARNING' | 'ALERT';

    /** 健康测量记录表单 */
    interface HealthMeasurementForm {
      /** 测量记录ID */
      measurementId?: string;
      /** 客户ID */
      elderId?: string;
      /** 服务日志ID */
      serviceLogId?: string;
      /** 测量类型 */
      measurementType: MeasurementType;
      /** 测量值 */
      measurementValue: string;
      /** 测量时间 */
      measuredAt?: string;
      /** 备注 */
      remark?: string;
    }

    /** 健康测量记录 */
    interface HealthMeasurement {
      /** 测量记录ID */
      measurementId: string;
      /** 客户ID */
      elderId: string;
      /** 客户姓名 */
      elderName?: string;
      /** 服务日志ID */
      serviceLogId?: string;
      /** 测量类型 */
      measurementType: MeasurementType;
      /** 测量类型名称 */
      measurementTypeName?: string;
      /** 测量值 */
      measurementValue: string;
      /** 单位 */
      measurementUnit?: string;
      /** 测量时间 */
      measuredAt: string;
      /** 测量人员ID */
      staffId?: string;
      /** 测量人员姓名 */
      staffName?: string;
      /** 备注 */
      remark?: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 健康测量统计 */
    interface HealthMeasurementStatistics {
      /** 测量类型 */
      measurementType: MeasurementType;
      /** 测量类型名称 */
      measurementTypeName?: string;
      /** 记录数量 */
      count: number;
      /** 最新测量值 */
      latestValue?: string;
      /** 最新测量时间 */
      latestTime?: string;
      /** 平均值 */
      averageValue?: number | string;
      /** 最大值 */
      maxValue?: number | string;
      /** 最小值 */
      minValue?: number | string;
      /** 趋势数据 */
      trendData?: Array<{ value: string; time: string }>;
      /** 预警状态 */
      alertStatus: AlertStatus;
      /** 预警信息 */
      alertMessage?: string;
    }

    /** 报告类型 */
    type ReportType = 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'SPECIAL';

    /** 健康报告生成参数 */
    interface HealthReportGenerate {
      /** 报告类型 */
      reportType: ReportType;
      /** 报告开始日期 */
      startDate?: string;
      /** 报告结束日期 */
      endDate?: string;
      /** 报告标题 */
      title?: string;
    }

    /** 健康报告 */
    interface HealthReport {
      /** 报告ID */
      reportId: string;
      /** 客户ID */
      elderId: string;
      /** 报告编号 */
      reportNo?: string;
      /** 报告日期 */
      reportDate?: string;
      /** 报告类型 */
      reportType: ReportType;
      /** 报告标题 */
      title?: string;
      /** 报告内容 */
      content?: string;
      /** PDF存储路径 */
      pdfUrl?: string;
      /** 员工ID */
      staffId?: string;
      /** 员工姓名 */
      staffName?: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 健康报告VO */
    interface HealthReportVO extends HealthReport {
      /** 客户姓名 */
      elderName?: string;
      /** 报告类型名称 */
      reportTypeName?: string;
    }

    /** 护理建议 */
    interface CareSuggestion {
      /** 建议类型 */
      type: string;
      /** 建议类型名称 */
      typeName: string;
      /** 建议内容 */
      content: string;
      /** 优先级 */
      priority: number;
      /** 依据 */
      basis: string;
    }

    /** 护理建议VO */
    interface CareSuggestionVO {
      /** 客户ID */
      elderId: string;
      /** 客户姓名 */
      elderName: string;
      /** 评估时间 */
      evaluateTime: string;
      /** 整体护理等级建议 */
      careLevelSuggestion: string;
      /** 护理建议列表 */
      suggestions: CareSuggestion[];
      /** 风险提示 */
      riskAlerts: string[];
    }

    /** 就医建议 */
    interface MedicalSuggestion {
      /** 建议类型 */
      type: string;
      /** 建议类型名称 */
      typeName: string;
      /** 建议内容 */
      content: string;
      /** 优先级 */
      priority: number;
      /** 依据 */
      basis: string;
    }

    /** 就医建议VO */
    interface MedicalSuggestionVO {
      /** 客户ID */
      elderId: string;
      /** 客户姓名 */
      elderName: string;
      /** 评估时间 */
      evaluateTime: string;
      /** 就医紧急程度 */
      urgencyLevel: string;
      /** 就医紧急程度名称 */
      urgencyLevelName: string;
      /** 就医科室建议 */
      suggestedDepartment: string;
      /** 就医建议列表 */
      suggestions: MedicalSuggestion[];
      /** 症状描述 */
      symptoms: string[];
    }

    /** 健康档案 */
    interface ElderHealth {
      healthId: string;
      elderId: string;
      bloodType?: number;
      height?: number;
      weight?: number;
      medicalHistory?: string;
      currentMedication?: string;
      allergyHistory?: string;
      chronicDiseases?: string;
      adlScore?: number;
      mmseScore?: number;
      gdsScore?: number;
      fallRisk?: number;
      pressureSoreRisk?: number;
      nutritionStatus?: number;
      visionStatus?: number;
      hearingStatus?: number;
      oralHealth?: number;
      mobilityStatus?: number;
      healthRemark?: string;
      checkupDate?: string;
      createTime?: string;
      updateTime?: string;
    }

    /** 健康档案表单 */
    interface ElderHealthDTO {
      healthStatus?: string;
      medicalHistory?: string;
      allergyHistory?: string;
      bloodType?: number;
      height?: number;
      weight?: number;
      currentMedication?: string;
      chronicDiseases?: string;
      healthRemark?: string;
    }

    /** 客户健康卡片（用于健康档案页面展示） */
    interface ElderHealthCard {
      /** 客户ID */
      elderId: string;
      /** 姓名 */
      name: string;
      /** 头像URL */
      photoUrl?: string;
      /** 年龄 */
      age?: number;
      /** 性别 */
      gender?: string;
      /** 性别名称 */
      genderName?: string;
      /** 护理等级 */
      careLevel?: string;
      /** 护理等级名称 */
      careLevelName?: string;
      /** 健康指数（0-100） */
      healthIndex?: number;
      /** 健康状态 */
      healthStatus?: string;
      /** 最新测量值 */
      latestMeasurementValue?: string;
      /** 最新测量时间 */
      latestMeasurementTime?: string;
      /** 最新测量类型 */
      latestMeasurementType?: string;
      /** 联系电话 */
      phone?: string;
    }
  }
}
