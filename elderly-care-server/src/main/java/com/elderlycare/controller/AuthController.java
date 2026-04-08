package com.elderlycare.controller;

import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.LoginDTO;
import com.elderlycare.dto.PasswordDTO;
import com.elderlycare.service.AuthService;
import com.elderlycare.vo.LoginVO;
import com.elderlycare.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String loginIp = getClientIp(request);
        LoginVO loginVO = authService.login(loginDTO, loginIp);
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        String userId = UserContext.getUserId();
        authService.logout(userId);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<UserInfoVO> getCurrentUserInfo() {
        String userId = UserContext.getUserId();
        UserInfoVO userInfo = authService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        String userId = UserContext.getUserId();
        passwordDTO.setUserId(userId);
        authService.changePassword(userId, passwordDTO);
        return Result.success();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
