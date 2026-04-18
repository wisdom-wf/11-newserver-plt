package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用户信息视图对象
 */
@Data
public class UserInfoVO implements Serializable {

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

    /** 租户ID */
    private String tenantId;

    /** 区域ID */
    private String areaId;

    /** 服务商ID */
    private String providerId;

    /** 角色列表 */
    private List<String> roles;

    /** 按钮权限列表（前端 hasAuth 使用） */
    private List<String> buttons;
}
