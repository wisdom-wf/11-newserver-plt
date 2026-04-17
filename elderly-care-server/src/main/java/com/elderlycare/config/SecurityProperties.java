package com.elderlycare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.aes")
public class SecurityProperties {

    /**
     * AES加密密钥
     */
    private String key = "ElderlyCarePwdKey2026Default";
}
