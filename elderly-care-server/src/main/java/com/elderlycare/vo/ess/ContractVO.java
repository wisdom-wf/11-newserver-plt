package com.elderlycare.vo.ess;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContractVO {

    private String contractId;

    private String contractNo;

    private String orderId;

    private String orderNo;

    private String flowId;

    private String contractName;

    private String signers;

    // 甲方（服务商）名称
    private String providerName;

    // 乙方（服务人员）名称
    private String staffName;

    private String status;

    private String statusText;

    private String signUrl;

    private LocalDateTime signedTime;

    private String downloadUrl;

    private LocalDateTime createTime;
}