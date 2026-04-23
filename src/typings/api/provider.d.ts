/**
 * 服务商管理模块
 */

declare namespace Api {
  namespace Provider {
    /** 服务商查询参数 */
    interface ProviderQuery {
      /** 服务商名称 */
      providerName?: string;
      /** 统一社会信用代码 */
      creditCode?: string;
      /** 服务商类型 */
      providerType?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商状态 */
      status?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 服务商表单 */
    interface ProviderForm {
      /** 服务商ID（编辑时需要） */
      providerId?: string;
      /** 服务商名称 */
      providerName: string;
      /** 统一社会信用代码 */
      creditCode?: string;
      /** 服务商类型 */
      providerType?: string;
      /** 法人姓名 */
      legalPerson?: string;
      /** 联系电话 */
      contactPhone?: string;
      /** 联系地址 */
      address?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 服务类型列表 */
      serviceTypes?: string[];
      /** 服务区域 */
      serviceAreas?: string[];
      /** 营业执照 */
      businessLicense?: string;
      /** 资质证书 */
      qualificationCerts?: string[];
      /** 简介 */
      description?: string;
      /** 状态 */
      status?: EnableStatus;
    }

    /** 服务商详情 - 与后端Provider实体对齐 */
    interface Provider {
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName: string;
      /** 服务商类型 */
      providerType?: string;
      /** 统一社会信用代码 */
      creditCode?: string;
      /** 法人姓名 */
      legalPerson?: string;
      /** 联系电话 */
      contactPhone?: string;
      /** 联系地址 */
      address?: string;
      /** 区域ID */
      areaId?: string;
      /** 区域名称 */
      areaName?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 服务类型列表 */
      serviceTypes?: ServiceType[];
      /** 服务区域 - 后端为逗号分隔字符串 */
      serviceAreas?: string | string[];
      /** 简介 */
      description?: string;
      /** 状态 */
      status: string;
      /** 平均评分 */
      rating?: number;
      /** 评分次数 */
      ratingCount?: number;
      /** 资质列表 */
      qualifications: Qualification[];
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 服务商服务类型 */
    interface ServiceType {
      /** 服务类型ID */
      providerServiceId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 服务类型编码 */
      serviceTypeCode?: string;
      /** 服务类型名称 */
      serviceTypeName?: string;
      /** 政府补贴价格 */
      subsidyPrice?: number;
      /** 市场价格 */
      servicePrice?: number;
      /** 服务区域 */
      serviceArea?: string;
      /** 状态 */
      status?: string;
    }

    /** 服务商统计 */
    interface Statistics {
      /** 总数 */
      totalProviders: number;
      /** 启用 */
      enabledProviders: number;
      /** 禁用 */
      disabledProviders: number;
    }

    /** 服务商资质证书 */
    interface Qualification {
      /** 资质ID */
      qualificationId: string;
      /** 服务商ID */
      providerId: string;
      /** 资质名称 */
      qualificationName: string;
      /** 资质类型 */
      qualificationType: string;
      /** 资质编号 */
      qualificationNumber: string;
      /** 有效期开始 */
      issueDate: string;
      /** 有效期截止 */
      expiryDate: string;
      /** 发证机构 */
      issueOrganization: string;
      /** 资质证书图片URL */
      attachmentUrl: string;
      /** 状态 */
      status: string;
      /** 审核状态 */
      auditStatus: string;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }
  }
}
