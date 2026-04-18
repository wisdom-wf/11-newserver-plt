# 变更说明：用户权限模块

## 变更时间
2026-04-18

## 变更概述

实现完整的三层权限控制体系（菜单级 + 按钮级 + 数据级），并修正了所有统计 API 的权限配置。

---

## 详细变更

### 1. 新增后端组件

#### PermissionInterceptor.java（新建）
- **路径**：`interceptor/PermissionInterceptor.java`
- **功能**：基于 URL + HTTP Method 的 API 权限校验拦截器
- **特性**：
  - 使用 AntPathMatcher 支持 `*` 通配符匹配
  - 超级管理员（R_SUPER）角色跳过所有检查
  - 权限格式：`METHOD:url`（如 `GET:/api/orders`）
  - 未匹配到任何权限时返回 403

#### UserContext.java（扩展）
- 新增 ThreadLocal 字段：`ROLES`、`PERMISSIONS`、`PROVIDER_ID`、`USER_TYPE`、`PERMISSION_URLS`
- 所有字段在 `clear()` 中正确清理

#### JwtAuthenticationInterceptor.java（扩展）
- 新增 `loadUserPermissionContext(userId)` 方法
- 登录后加载用户角色、权限码、权限URL列表、用户类型、所属服务商ID

### 2. WebMvcConfig.java（修改）
- **修正**：excludePathPatterns 从错误的 `/api/system/auth/*` 改为 `/api/auth/*`
- **新增**：排除 `/api/auth/userinfo`（所有登录用户可访问自己的用户信息）

### 3. 数据模型扩展

#### User.java
- 新增 `providerId` 字段
- 新增 `areaId` 字段

#### UserInfoVO.java
- `permissions` 字段更名为 `buttons`（对齐前端 Api.Auth.UserInfo）
- 新增 `providerId`、`areaId` 字段

#### UserDTO.java
- 新增 `providerId`、`areaId` 字段

#### ElderPageDTO.java
- 新增 `providerId` 字段

### 4. Service/Mapper 层

#### UserMapper.xml
- 新增 `selectPermissionUrlsByUserId` SQL
- 返回格式：`METHOD:url`（如 `GET:/api/orders`）

#### ElderMapper.xml
- 补充 `provider_id` WHERE 条件过滤

#### 8个 Controller 新增数据权限自动注入
- OrderController、AppointmentController、ServiceLogController、QualityCheckController
- StaffController、EvaluationController、CockpitController、ElderController
- 逻辑：Provider Admin 角色自动注入 providerId，限制只能查看自己服务商的数据

### 5. 权限种子数据

#### permission_menu_seed.sql（已修改）
- 补充 4 个驾驶舱按钮权限（cockpit:overview:query 等）
- 新增 R002（服务商管理员）角色，关联 69 个业务权限
- 新增 R003（服务人员）角色，关联 16 个只读+服务日志权限
- R001（超级管理员）关联全部 105 个权限

#### permission_url_fix.sql（新建）
- 修正 appointment/quality-check/service-log 的 URL（与后端 Controller 一致）
- 新增 40+ 缺失的按钮权限（统计 API、子操作 API）
- 分配新增权限给超级管理员

### 6. 前端变更

#### 已添加 hasAuth 保护的页面
- `views/business/elder/index.vue` - 新增/编辑/删除按钮
- `views/business/order/index.vue` - 派单/取消按钮
- `views/business/staff/index.vue` - 新增/编辑/删除按钮
- `views/provider/index.vue` - 新增/编辑/删除按钮
- `views/business/service-log/index.vue` - 更新按钮
- `views/appointment/index.vue` - 确认按钮

### 7. 文档

#### permission_rbac_guide.md（新建）
- 权限模块完整配置指南
- 角色定义、权限结构规范
- 新增权限开发流程
- 常见问题排查

---

## 数据库变更

```sql
-- t_user 表新增字段（由 seed SQL 执行）
ALTER TABLE t_user ADD COLUMN provider_id VARCHAR(32);
ALTER TABLE t_user ADD COLUMN area_id VARCHAR(32);

-- 需要执行初始化 SQL
elderly-care-server/sql/permission_menu_seed.sql
elderly-care-server/sql/permission_url_fix.sql
```

---

## 验证结果

### 权限矩阵（已验证）
| API | Super Admin | Provider Admin | Staff |
|-----|:-----------:|:--------------:|:-----:|
| System Users | 200 | 403 | 403 |
| Orders | 200 | 200 | 200 |
| Staff Management | 200 | 200 | 403 |
| Elders | 200 | 200 | 403 |
| Cockpit | 200 | 200 | 200 |

### 数据隔离（已验证）
| 角色 | 订单总数 | 老人总数 |
|------|:-------:|:-------:|
| Super Admin | 21 | 14 |
| Provider Admin | 5 | 5 |

---

## 后续增量开发原则

### 新增 API 权限流程
1. **后端**：实现 Controller
2. **数据库**：在 t_permission 插入按钮权限记录（permission_url + permission_method）
3. **角色**：在 t_role_permission 分配给对应角色
4. **前端**：使用 `hasAuth('code')` 保护操作按钮

### 新增数据权限流程
1. **确认**：该接口是否需要按 providerId 隔离数据
2. **检查**：UserContext.getProviderId() 是否有值
3. **注入**：在 Controller 方法中添加 autoPid 注入逻辑
4. **Mapper**：确保 XML 中有 `AND provider_id = #{providerId}` 条件

### 前端新增菜单/按钮流程
1. **路由**：在 routes.ts 中添加路由，meta.roles 控制可见性
2. **权限码**：在 seed SQL 中定义菜单/按钮权限码
3. **按钮保护**：使用 `hasAuth('code')` 包装操作按钮
4. **角色分配**：在 t_role_permission 中分配菜单权限给角色
