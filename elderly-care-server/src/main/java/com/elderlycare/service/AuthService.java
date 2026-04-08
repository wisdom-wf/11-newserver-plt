package com.elderlycare.service;

import com.elderlycare.dto.LoginDTO;
import com.elderlycare.dto.PasswordDTO;
import com.elderlycare.vo.LoginVO;
import com.elderlycare.vo.UserInfoVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO, String loginIp);

    /**
     * 用户登出
     */
    void logout(String userId);

    /**
     * 获取当前用户信息
     */
    UserInfoVO getCurrentUserInfo(String userId);

    /**
     * 修改密码
     */
    void changePassword(String userId, PasswordDTO passwordDTO);

    /**
     * 重置密码
     */
    void resetPassword(String userId, String newPassword);
}
