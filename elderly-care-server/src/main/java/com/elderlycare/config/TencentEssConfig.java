package com.elderlycare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.ess")
public class TencentEssConfig {

    /**
     * 腾讯云 SecretId
     */
    private String secretId;

    /**
     * 腾讯云 SecretKey
     */
    private String secretKey;

    /**
     * 应用ID（代理应用）
     */
    private String agentId;

    /**
     * 地域
     */
    private String region = "ap-guangzhou";

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * 默认过期天数
     */
    private Integer defaultExpireDays = 7;

    /**
     * 合同模板ID（腾讯电子签控制台创建）
     */
    private String templateId;

    /**
     * 操作人UserId（企业管理员在电子签系统的UserId）
     */
    private String operatorId;
}
