package com.elderlycare.service.dispute;

import com.elderlycare.common.PageResult;
import com.elderlycare.entity.dispute.ServiceDispute;

/**
 * 服务争议服务接口
 */
public interface ServiceDisputeService {

    /**
     * 创建争议申请
     */
    String createDispute(ServiceDispute dispute);

    /**
     * 获取争议列表
     */
    PageResult<ServiceDispute> getDisputeList(String providerId, String disputeType, String disputeStatus, int page, int pageSize);

    /**
     * 获取争议详情
     */
    ServiceDispute getDisputeById(String disputeId);

    /**
     * 开始调查
     */
    void startInvestigation(String disputeId, String investigationContent);

    /**
     * 进行调解
     */
    void mediate(String disputeId, String mediationContent);

    /**
     * 达成协议
     */
    void reachAgreement(String disputeId, String agreementContent);

    /**
     * 关闭争议
     */
    void closeDispute(String disputeId, String closeReason);
}
