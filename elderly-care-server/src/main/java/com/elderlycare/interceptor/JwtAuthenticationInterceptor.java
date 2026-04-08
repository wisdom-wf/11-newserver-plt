package com.elderlycare.interceptor;

import com.elderlycare.common.JwtUtil;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证拦截器
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 不需要认证的路径 */
    private static final String[] EXCLUDE_PATHS = {
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/captcha",
            "/swagger-ui",
            "/v3/api-docs",
            "/doc.html",
            "/webjars"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();

        // 检查是否是不需要认证的路径
        for (String path : EXCLUDE_PATHS) {
            if (requestUri.contains(path)) {
                return true;
            }
        }

        // 获取Token
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            sendUnauthorizedResponse(response, "未提供认证令牌");
            return false;
        }

        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorizedResponse(response, "无效或过期的认证令牌");
            return false;
        }

        // 检查Token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            sendUnauthorizedResponse(response, "认证令牌已过期");
            return false;
        }

        // 解析用户信息并存入请求属性和上下文
        Claims claims = jwtUtil.parseToken(token);
        String userId = claims.get("userId", String.class);
        String username = claims.get("username", String.class);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        UserContext.setUserId(userId);
        UserContext.setUsername(username);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理上下文
        UserContext.clear();
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Result<Void> result = Result.unauthorized(message);
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
    }
}
