package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String userName;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别 */
    private String gender;

    /** 头像URL */
    private String avatar;

    /** 用户类型 */
    private String userType;

    /** 状态 */
    private String status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 租户ID */
    private String tenantId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 角色列表 */
    private List<RoleVO> roles;
}
