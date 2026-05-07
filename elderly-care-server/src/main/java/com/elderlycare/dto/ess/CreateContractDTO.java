package com.elderlycare.dto.ess;

import lombok.Data;

import java.util.List;

@Data
public class CreateContractDTO {

    private String orderId;

    private String staffId;

    private String contractName;

    private List<SignerInfo> signers;

    @Data
    public static class SignerInfo {
        private String name;
        private String mobile;
        private Integer type; // 0=个人, 1=企业
    }
}