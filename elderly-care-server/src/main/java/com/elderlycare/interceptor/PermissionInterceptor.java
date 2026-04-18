package com.elderlycare.interceptor;

import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.List;

/**
 * 权限校验拦截器
 * 基于 URL + HTTP Method 匹配 t_permission 中的 permission_url + permission_method
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 超级管理员跳过权限检查
        List<String> roles = UserContext.getRoles();
        if (roles != null && roles.contains("R_SUPER")) {
            return true;
        }

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 检查用户权限列表中是否有匹配的 URL+Method
        List<String> permissionUrls = UserContext.getPermissionUrls();
        if (permissionUrls != null) {
            for (String permEntry : permissionUrls) {
                // permEntry 格式: "GET:/api/orders" 或 "PUT:/api/orders/*/cancel"
                String[] parts = permEntry.split(":", 2);
                if (parts.length != 2) continue;

                String permMethod = parts[0];
                String permUrl = parts[1];

                // 方法匹配（忽略大小写）
                if (!permMethod.equalsIgnoreCase(requestMethod)) continue;

                // URL 匹配（支持 Ant 风格通配符）
                if (pathMatcher.match(permUrl, requestUri)) {
                    return true;
                }
            }
        }

        // 无权限
        sendForbiddenResponse(response, "无权限访问该资源");
        return false;
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Result<Void> result = Result.forbidden(message);
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
    }
}
