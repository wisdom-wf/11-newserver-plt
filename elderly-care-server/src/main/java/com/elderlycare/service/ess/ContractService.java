package com.elderlycare.service.ess;

import com.elderlycare.dto.ess.ContractQueryDTO;
import com.elderlycare.vo.ess.ContractVO;
import com.elderlycare.vo.ess.SignUrlVO;

import java.util.List;

public interface ContractService {

    /**
     * 创建服务合同（派单时自动调用）
     */
    ContractVO createServiceContract(String orderId, String staffId);

    /**
     * 获取订单关联的合同
     */
    ContractVO getContractByOrderId(String orderId);

    /**
     * 获取签署链接
     */
    SignUrlVO getSignUrl(String contractId, Integer approverType);

    /**
     * 检查合同是否已签署
     */
    boolean isContractSigned(String orderId);

    /**
     * 查询合同状态
     */
    String getContractStatus(String contractId);

    /**
     * 处理腾讯电子签回调
     */
    void handleCallback(Object callbackData);

    /**
     * 下载合同
     */
    String downloadContract(String contractId);

    /**
     * 获取合同列表
     */
    List<ContractVO> getContractList(ContractQueryDTO queryDTO);
}