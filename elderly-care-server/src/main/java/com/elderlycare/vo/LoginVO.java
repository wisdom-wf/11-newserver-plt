package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录响应视图对象
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    private String accessToken;

    /** 令牌类型 */
    private String tokenType;

    /** 过期时间(秒) */
    private Long expiresIn;

    /** 用户信息 */
    private UserInfoVO userInfo;
}
