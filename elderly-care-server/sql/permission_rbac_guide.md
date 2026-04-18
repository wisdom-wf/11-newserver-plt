# 权限模块配置指南

## 概述

本系统采用 RBAC（Role-Based Access Control）模型实现三层权限控制：
- **菜单级**：路由可见性控制（前端 meta.roles）
- **按钮级**：API 访问控制（PermissionInterceptor）
- **数据级**：providerId 数据隔离（Service 层自动注入）

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `t_user` | 用户表，含 userType/providerId/areaId |
| `t_role` | 角色表，含 roleType（SYSTEM/BUSINESS）/dataScope |
| `t_permission` | 权限表，含 permission_url/permission_method |
| `t_user_role` | 用户-角色关联 |
| `t_role_permission` | 角色-权限关联 |

## 角色定义

### R001 - 超级管理员 (SUPER_ADMIN)
- 拥有所有权限（BUTTON + MENU）
- 不经过 PermissionInterceptor 检查
- 可见全部数据

### R002 - 服务商管理员 (PROVIDER_ADMIN)
- 拥有业务模块全部权限（不含 system*/financial*）
- 数据范围：仅限自己 providerId 下的数据
- userType = PROVIDER

### R003 - 服务人员 (STAFF)
- 仅读权限 + 服务日志操作
- 数据范围：仅限自己 providerId 下的数据
- userType = STAFF

## 权限结构规范

### permission_type
- `MENU`：菜单节点，permission_url/permission_method 为 NULL
- `BUTTON`：按钮/API 权限，permission_url + permission_method 必须填写

### permission_url 格式
- 精确路径：`/api/orders`
- 路径参数：`/api/orders/*/cancel`（* 匹配任意路径段）

### permission_method
- HTTP 方法：GET, POST, PUT, DELETE

### 特殊约定
- 前缀带 `*` 的 URL 表示 AntPathMatcher 匹配，如 `/api/providers/*/audit`
- 前缀带 `**` 的 URL 表示 AntPathMatcher 匹配剩余路径

## 数据权限自动注入

### 实现方式
在 Controller 方法中，从 UserContext 获取 providerId 并注入到查询 DTO：

```java
String autoPid = UserContext.getProviderId();
if (autoPid != null) {
    queryDTO.setProviderId(autoPid);
}
```

### 已实现自动注入的接口
- `/api/orders` - OrderController.getOrderList()
- `/api/appointments` - AppointmentController.getAppointmentList()
- `/api/service-logs` - ServiceLogController.getServiceLogList()
- `/api/quality-checks` - QualityCheckController.getQualityCheckList()
- `/api/evaluations` - EvaluationController.queryEvaluations()
- `/api/elders` - ElderController.getElderPage()
- `/api/staff` - StaffController.queryStaff()
- `/api/cockpit/*` - CockpitController 各方法

## 新增权限时的开发流程

### 1. 后端 - 添加 Controller 权限注解
无需额外注解，通过 PermissionInterceptor 的 URL 匹配自动控制。

### 2. 数据库 - 插入权限记录
```sql
INSERT INTO t_permission (permission_id, permission_code, permission_name,
  permission_type, parent_id, permission_url, permission_method, sort_order,
  icon, status, create_time, deleted)
VALUES ('Pxxxxxxxxx', 'module:resource:action', '操作名称', 'BUTTON',
  'Pxxxxx', '/api/module/*/action', 'PUT', 1, NULL, 'NORMAL', NOW(), 0);
```

### 3. 分配权限给角色
```sql
-- 超级管理员拥有所有权限
INSERT INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP1_', permission_id), 'R001', permission_id, NOW()
FROM t_permission WHERE permission_code = 'module:resource:action';

-- 服务商管理员也需要此权限
INSERT INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP2_', permission_id), 'R002', permission_id, NOW()
FROM t_permission WHERE permission_code = 'module:resource:action'
  AND NOT EXISTS (SELECT 1 FROM t_role_permission WHERE role_id='R002' AND permission_id=(SELECT permission_id FROM t_permission WHERE permission_code='module:resource:action'));
```

### 4. 前端 - hasAuth 按钮保护
```vue
<NButton v-if="hasAuth('module:resource:action')" @click="handleAction">
  操作名称
</NButton>
```

### 5. 前端 - 路由权限控制
路由 meta.roles 自动过滤：
```ts
{
  path: '/business/order',
  meta: { roles: ['SUPER_ADMIN', 'PROVIDER_ADMIN'] }
}
```

## 种子数据文件

| 文件 | 用途 |
|------|------|
| `sql/permission_menu_seed.sql` | 完整权限树 + 3个角色 + 角色-权限映射 |
| `sql/permission_url_fix.sql` | URL 修正 + 缺失按钮权限补充 |

## 拦截器配置

### WebMvcConfig.java 排除路径
```
/api/auth/login          - 登录（无需认证）
/api/auth/logout         - 登出（无需认证）
/api/auth/userinfo       - 获取当前用户信息（已登录即可访问）
```

## 常见问题排查

### 1. 登录返回 403 "无权限访问该资源"
检查 WebMvcConfig 中是否将登录接口加入了 excludePathPatterns。

### 2. API 返回 403
- 检查 t_permission 中是否存在该 URL + Method 的权限记录
- 检查用户的角色是否关联了该权限
- 超级管理员（R_SUPER）角色跳过检查

### 3. 数据未隔离
- 检查 UserContext.getProviderId() 是否返回了 providerId
- 检查 Controller 是否调用了 setProviderId() 自动注入

### 4. 前端按钮未隐藏但后端返回 403
hasAuth 只控制前端按钮显示，不能防护绕过。可直接调用 API 的用户仍会被后端拦截。
