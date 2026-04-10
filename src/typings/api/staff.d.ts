/**
 * 服务人员管理模块
 */

declare namespace Api {
  namespace Staff {
    /** 服务人员查询参数 */
    interface StaffQuery {
      /** 姓名 */
      name?: string;
      /** 手机号 */
      phone?: string;
      /** 身份证号 */
      idCard?: string;
      /** 服务类别 */
      serviceCategory?: string;
      /** 服务类型 */
      serviceType?: string;
      /** 服务商ID */
      providerId?: string;
      /** 区域ID */
      areaId?: string;
      /** 状态 */
      status?: string;
    }

    /** 服务人员表单 */
    interface StaffForm {
      /** 服务人员ID（编辑时需要） */
      id?: string;
      /** 姓名 */
      name: string;
      /** 性别 */
      gender?: Gender;
      /** 出生日期 */
      birthDate?: string;
      /** 身份证号 */
      idCard?: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      avatar?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 服务类型列表 */
      serviceTypes?: string[];
      /** 服务区域 */
      serviceAreas?: string[];
      /** 所属服务商ID */
      providerId: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 健康证 */
      healthCert?: string;
      /** 资质证书 */
      qualificationCerts?: string[];
      /** 简介 */
      description?: string;
      /** 状态 */
      status?: EnableStatus;
    }

    /** 服务人员详情 */
    interface Staff {
      /** 服务人员ID */
      id: string;
      /** 工号 */
      staffNo?: string;
      /** 姓名 */
      name: string;
      /** 性别 */
      gender?: Gender;
      /** 出生日期 */
      birthDate?: string;
      /** 年龄 */
      age?: number;
      /** 身份证号 */
      idCard?: string;
      /** 手机号 */
      phone?: string;
      /** 头像 */
      avatar?: string;
      /** 服务类别 */
      serviceCategory: ServiceCategory;
      /** 服务类型列表 */
      serviceTypes?: string[];
      /** 服务区域 */
      serviceAreas?: string[];
      /** 所属服务商ID */
      providerId: string;
      /** 所属服务商名称 */
      providerName?: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系电话 */
      emergencyPhone?: string;
      /** 健康证 */
      healthCert?: string;
      /** 资质证书 */
      qualificationCerts?: string[];
      /** 简介 */
      description?: string;
      /** 状态 */
      status: EnableStatus;
      /** 评分 */
      rating?: number;
      /** 接单数 */
      orderCount?: number;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 性别 */
    type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN';

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
  }
}
