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

    /** 老人详情 */
    interface Elder {
      /** 老人ID */
      elderId: string;
      /** 档案编号 */
      fileNo?: string;
      /** 姓名 - 与后端elder_name字段映射 */
      name: string;
      /** 老人姓名 - 兼容其他模块使用的elderName */
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

    /** 老人统计 */
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
    }
  }
}
