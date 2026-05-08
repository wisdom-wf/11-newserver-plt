package com.elderlycare.service.ess;

import com.elderlycare.common.PageResult;
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
     * 根据合同ID获取合同
     */
    ContractVO getContractById(String contractId);

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
     * 取消合同
     */
    void cancelContract(String contractId);

    /**
     * 获取合同列表（分页）
     */
    PageResult<ContractVO> getContractList(ContractQueryDTO queryDTO);
}