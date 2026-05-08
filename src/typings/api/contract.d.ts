/**
 * 合同管理模块
 */

declare namespace Api {
  namespace Ess {
    /** 合同 */
    interface Contract {
      contractId: string;
      contractNo: string;
      orderId: string;
      orderNo: string;
      flowId: string;
      contractName: string;
      signers: string;
      /** 甲方（服务商）名称 */
      providerName?: string;
      /** 乙方（服务人员）名称 */
      staffName?: string;
      status: string;
      statusText?: string;
      signUrl?: string;
      signedTime?: string;
      downloadUrl?: string;
      createTime?: string;
    }

    /** 签署链接 */
    interface SignUrl {
      signUrl: string;
      expireTime: string;
    }

    /** 合同查询参数 */
    interface ContractQuery {
      contractNo?: string;
      status?: string;
      startDate?: string;
      endDate?: string;
      page?: number;
      pageSize?: number;
    }
  }
}
