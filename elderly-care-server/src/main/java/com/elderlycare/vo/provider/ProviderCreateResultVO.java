package com.elderlycare.vo.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务商创建结果VO（包含自动创建的管理员账号信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCreateResultVO {

    /** 服务商ID */
    private String providerId;

    /** 服务商名称 */
    private String providerName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 自动创建的管理员账号 */
    private AccountInfo autoCreatedAccount;

    /**
     * 管理员账号信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        /** 用户名 */
        private String username;

        /** 初始密码 */
        private String password;

        /** 角色名称 */
        private String roleName;
    }
}
