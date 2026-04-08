package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志视图对象
 */
@Data
public class LoginLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录日志ID */
    private String loginLogId;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 登录IP */
    private String loginIp;

    /** 登录位置 */
    private String loginLocation;

    /** 登录设备 */
    private String loginDevice;

    /** 登录状态 */
    private String loginStatus;

    /** 失败原因 */
    private String failReason;
}
