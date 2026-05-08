package com.elderlycare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "contract.enterprise")
public class ContractEnterpriseConfig {

    /** 企业名称 */
    private String name;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 法定代表人 */
    private String legalPerson;

    /** 企业地址 */
    private String address;

    /** 企业联系电话 */
    private String phone;

    /** 企业印章ID（腾讯电子签） */
    private String sealId;

    /** 企业电子签OpenId（腾讯） */
    private String essOpenId;
}
