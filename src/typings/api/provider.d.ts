/**
 * 服务商管理模块
 */

declare namespace Api {
  namespace Provider {
    /** 服务商查询参数 */
    interface ProviderQuery {
      /** 服务商名称 */
      name?: string;
      /** 统一社会信用代码 */
      creditCode?: string;
      /** 服务类型 */
      serviceType?: string;
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
      id?: string;
      /** 服务商名称 */
      name: string;
      /** 统一社会信用代码 */
      creditCode?: string;
      /** 法人姓名 */
      legalPerson?: string;
      /** 联系电话 */
      phone?: string;
      /** 联系地址 */
      address?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
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

    /** 服务商详情 */
    interface Provider {
      /** 服务商ID */
      id: string;
      /** 服务商名称 */
      name: string;
      /** 统一社会信用代码 */
      creditCode?: string;
      /** 法人姓名 */
      legalPerson?: string;
      /** 联系电话 */
      phone?: string;
      /** 联系地址 */
      address?: string;
      /** 区域ID */
      areaId?: string;
      /** 区域名称 */
      areaName?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
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
      status: EnableStatus;
      /** 审核状态 */
      auditStatus?: AuditStatus;
      /** 审核备注 */
      auditRemark?: string;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 服务商统计 */
    interface Statistics {
      /** 总数 */
      total: number;
      /** 已认证 */
      certified: number;
      /** 认证中 */
      certifying: number;
      /** 已暂停 */
      suspended: number;
    }

    /** 审核状态 */
    type AuditStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
  }
}
