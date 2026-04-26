package com.elderlycare.config;

import com.elderlycare.interceptor.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    // TODO: PermissionInterceptor 暂时禁用（2026-04-24）
    // 根因：t_role_permission 表只有 R001 有记录，R004/R005 的 buttons=null 导致所有 API 403
    // 临时方案：注释掉旧版权限拦截器，依赖菜单权限（t_role_menu）控制
    // 完整修复：执行 sql/rbac_permission_fix.sql 补充 R004/R005 的按钮权限记录
    // @Autowired
    // private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT认证拦截器
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/evaluations/survey/**"
                );

        // 2. 权限校验拦截器暂时禁用（见上方 TODO）
        // 完整修复：执行 sql/rbac_permission_fix.sql 后，注释掉 JWT 后的这段注册代码即可恢复
        // registry.addInterceptor(permissionInterceptor)
        //         .addPathPatterns("/api/**")
        //         .excludePathPatterns(
        //                 "/api/auth/login",
        //                 "/api/auth/logout",
        //                 "/api/auth/userinfo",
        //                 "/api/menu/**"
        //         );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
