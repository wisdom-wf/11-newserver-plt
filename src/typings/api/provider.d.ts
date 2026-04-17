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
      serviceTypes?: string[];
      /** 服务区域 - 后端为逗号分隔字符串 */
      serviceAreas?: string | string[];
      /** 营业执照 */
      businessLicense?: string;
      /** 资质证书 */
      qualificationCerts?: string[];
      /** 简介 */
      description?: string;
      /** 状态 */
      status: string;
      /** 平均评分 - 后端新增字段 */
      rating?: number;
      /** 评分次数 - 后端新增字段 */
      ratingCount?: number;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
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
  }
}
