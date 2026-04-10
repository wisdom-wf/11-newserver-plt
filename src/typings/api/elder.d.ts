/**
 * 老人档案管理模块
 */

declare namespace Api {
  namespace Elder {
    /** 老人查询参数 */
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

    /** 老人表单 */
    interface ElderForm {
      /** 老人ID（编辑时需要） */
      id?: string;
      /** 姓名 */
      name: string;
      /** 性别 */
      gender?: Gender;
      /** 出生日期 */
      birthDate?: string;
      /** 年龄 */
      age?: number;
      /** 身份证号 */
      idCard: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      avatar?: string;
      /** 区域ID */
      areaId?: string;
      /** 社区ID */
      communityId?: string;
      /** 详细地址 */
      address?: string;
      /** 养老类型 */
      careType?: CareType;
      /** 补贴类型 */
      subsidyType?: SubsidyType;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 健康状况 */
      healthStatus?: HealthStatus;
      /** 既往病史 */
      medicalHistory?: string[];
      /** 过敏信息 */
      allergies?: string[];
      /** 护理等级 */
      careLevel?: CareLevel;
      /** 民族 */
      ethnicity?: string;
      /** 婚姻状况 */
      maritalStatus?: MaritalStatus;
      /** 退休单位 */
      retiredFrom?: string;
      /** 养老金 */
      pension?: number;
      /** 备注 */
      remark?: string;
    }

    /** 老人详情 */
    interface Elder {
      /** 老人ID */
      id: string;
      /** 档案编号 */
      fileNo?: string;
      /** 姓名 */
      name: string;
      /** 性别 */
      gender?: Gender;
      /** 出生日期 */
      birthDate?: string;
      /** 年龄 */
      age?: number;
      /** 身份证号 */
      idCard: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      avatar?: string;
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
      careType?: CareType;
      /** 补贴类型 */
      subsidyType?: SubsidyType;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 健康状况 */
      healthStatus?: HealthStatus;
      /** 既往病史 */
      medicalHistory?: string[];
      /** 过敏信息 */
      allergies?: string[];
      /** 护理等级 */
      careLevel?: CareLevel;
      /** 民族 */
      ethnicity?: string;
      /** 婚姻状况 */
      maritalStatus?: MaritalStatus;
      /** 退休单位 */
      retiredFrom?: string;
      /** 养老金 */
      pension?: number;
      /** 备注 */
      remark?: string;
      /** 状态 */
      status?: EnableStatus;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 性别 */
    type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN';

    /** 养老类型 */
    type CareType = 'HOME' | 'COMMUNITY' | 'INSTITUTION';

    /** 补贴类型 */
    type SubsidyType = 'FULL_SUBSIDY' | 'PARTIAL_SUBSIDY' | 'SELF_PAY';

    /** 健康状况 */
    type HealthStatus = 'GOOD' | 'FAIR' | 'POOR' | 'CRITICAL';

    /** 护理等级 */
    type CareLevel = 'LEVEL_1' | 'LEVEL_2' | 'LEVEL_3' | 'LEVEL_4' | 'LEVEL_5';

    /** 婚姻状况 */
    type MaritalStatus = 'MARRIED' | 'WIDOWED' | 'DIVORCED' | 'SINGLE';

    /** 老人统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 在册 */
      registered: number;
      /** 暂停服务 */
      suspended: number;
      /** 养老类型分布 */
      careTypeStats: Record<CareType, number>;
      /** 护理等级分布 */
      careLevelStats: Record<CareLevel, number>;
      /** 补贴类型分布 */
      subsidyTypeStats: Record<SubsidyType, number>;
    }
  }
}
