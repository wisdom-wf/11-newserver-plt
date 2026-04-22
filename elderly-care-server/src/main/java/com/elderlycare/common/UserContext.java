package com.elderlycare.common;

import java.util.List;

/**
 * 用户上下文持有器
 * 用于在请求中存储和获取当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> PERMISSIONS = new ThreadLocal<>();
    private static final ThreadLocal<String> PROVIDER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<String> STAFF_ID = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> PERMISSION_URLS = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setRoles(List<String> roles) {
        ROLES.set(roles);
    }

    public static List<String> getRoles() {
        return ROLES.get();
    }

    public static void setPermissions(List<String> permissions) {
        PERMISSIONS.set(permissions);
    }

    public static List<String> getPermissions() {
        return PERMISSIONS.get();
    }

    public static void setProviderId(String providerId) {
        PROVIDER_ID.set(providerId);
    }

    public static String getProviderId() {
        return PROVIDER_ID.get();
    }

    public static void setUserType(String userType) {
        USER_TYPE.set(userType);
    }

    public static String getUserType() {
        return USER_TYPE.get();
    }

    public static void setStaffId(String staffId) {
        STAFF_ID.set(staffId);
    }

    public static String getStaffId() {
        return STAFF_ID.get();
    }

    public static void setPermissionUrls(List<String> permissionUrls) {
        PERMISSION_URLS.set(permissionUrls);
    }

    public static List<String> getPermissionUrls() {
        return PERMISSION_URLS.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
        PERMISSIONS.remove();
        PROVIDER_ID.remove();
        USER_TYPE.remove();
        STAFF_ID.remove();
        PERMISSION_URLS.remove();
    }
}
