package com.elderlycare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.ess")
public class TencentEssConfig {

    private String secretId;

    private String secretKey;

    private String agentId;

    private String region = "ap-guangzhou";

    private String callbackUrl;

    private Integer defaultExpireDays = 7;
}