package com.elderlycare.vo.staff;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务人员创建结果VO（包含账户信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffCreateResultVO extends StaffVO {

    /**
     * 系统账户用户名
     */
    private String username;

    /**
     * 初始密码（仅创建时返回）
     */
    private String password;

    /**
     * 是否已创建账户
     */
    private Boolean accountCreated;
}
