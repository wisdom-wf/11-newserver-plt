/**
 * 财务结算管理模块
 */

declare namespace Api {
  namespace Financial {
    /** 结算查询参数 */
    interface SettlementQuery {
      /** 结算单号 */
      settlementNo?: string;
      /** 订单号 */
      orderNo?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 结算状态 */
      status?: string;
      /** 结算类型 */
      settlementType?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 结算表单 */
    interface SettlementForm {
      /** 结算ID（编辑时需要） */
      id?: string;
      /** 订单ID */
      orderId: string;
      /** 结算金额 */
      amount: number;
      /** 结算类型 */
      settlementType: SettlementType;
      /** 结算方式 */
      paymentMethod?: PaymentMethod;
      /** 备注 */
      remark?: string;
    }

    /** 结算详情 */
    interface Settlement {
      /** 结算ID */
      id: string;
      /** 结算单号 */
      settlementNo: string;
      /** 订单ID */
      orderId: string;
      /** 订单号 */
      orderNo?: string;
      /** 服务类别 */
      serviceCategory?: ServiceCategory;
      /** 服务类型 */
      serviceType?: string;
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName?: string;
      /** 服务人员ID */
      staffId?: string;
      /** 服务人员姓名 */
      staffName?: string;
      /** 老人ID */
      elderId: string;
      /** 老人姓名 */
      elderName?: string;
      /** 服务费 */
      serviceFee?: number;
      /** 补贴金额 */
      subsidyFee?: number;
      /** 自付金额 */
      selfPayFee?: number;
      /** 结算金额 */
      amount: number;
      /** 结算类型 */
      settlementType: SettlementType;
      /** 结算方式 */
      paymentMethod?: PaymentMethod;
      /** 结算状态 */
      status: SettlementStatus;
      /** 结算周期 */
      settlementPeriod?: string;
      /** 支付时间 */
      paymentTime?: string;
      /** 备注 */
      remark?: string;
      /** 创建时间 */
      createTime: string;
      /** 更新时间 */
      updateTime?: string;
    }

    /** 结算类型 */
    type SettlementType = 'SERVICE_FEE' | 'SUBSIDY' | 'SELF_PAY';

    /** 结算状态 */
    type SettlementStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

    /** 支付方式 */
    type PaymentMethod = 'WECHAT' | 'ALIPAY' | 'CASH' | 'BANK_TRANSFER' | 'OTHER';

    /** 发票查询参数 */
    interface InvoiceQuery {
      /** 发票号 */
      invoiceNo?: string;
      /** 订单号 */
      orderNo?: string;
      /** 发票类型 */
      invoiceType?: string;
      /** 开票状态 */
      status?: string;
      /** 区域ID */
      areaId?: string;
      /** 服务商ID */
      providerId?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 发票详情 */
    interface Invoice {
      /** 发票ID */
      id: string;
      /** 发票号 */
      invoiceNo: string;
      /** 订单ID */
      orderId: string;
      /** 订单号 */
      orderNo?: string;
      /** 服务商ID */
      providerId: string;
      /** 服务商名称 */
      providerName?: string;
      /** 老人ID */
      elderId: string;
      /** 老人姓名 */
      elderName?: string;
      /** 发票类型 */
      invoiceType: InvoiceType;
      /** 发票金额 */
      amount: number;
      /** 发票状态 */
      status: InvoiceStatus;
      /** 开票时间 */
      invoiceTime?: string;
      /** 备注 */
      remark?: string;
      /** 创建时间 */
      createTime: string;
    }

    /** 发票类型 */
    type InvoiceType = 'VAT' | 'ELECTRONIC' | '普通发票';

    /** 发票状态 */
    type InvoiceStatus = 'PENDING' | 'ISSUED' | 'VOID';

    /** 财务统计 */
    interface Statistics {
      /** 待结算数 */
      pending: number;
      /** 已结算数 */
      completed: number;
      /** 本月结算金额 */
      monthAmount: number;
      /** 累计结算金额 */
      totalAmount: number;
      /** 服务费统计 */
      serviceFeeTotal: number;
      /** 补贴统计 */
      subsidyTotal: number;
      /** 自付统计 */
      selfPayTotal: number;
    }
  }
}
