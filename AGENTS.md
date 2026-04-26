# AGENTS.md - Hermes 项目说明

> 本文档供 Hermes Agent 接手项目使用
> 最后更新：2026-04-26

---

## 项目概述

**项目名称**：智慧居家养老服务管理平台  
**技术栈**：Spring Boot + Vue3 (NaiveUI) + MySQL + Docker  
**端口**：前端 **9527**，后端 **8080**  
**仓库**：https://github.com/wisdom-wf/11-newserver-plt  

---

## 最近完成的工作（2026-04-21 ~ 2026-04-25）

1. ✅ 服务日志 API 修复（返回值、404处理）
2. ✅ 首页统计数值 null 化修复（无数据显示"--"）
3. ✅ 服务商排名 SQL 修复（使用评价表计算平均分）
4. ✅ 全面 Playwright UI 审计（15个页面，30/31通过）
5. ✅ 前端配置修复（baseURL、选择器）
6. ✅ 父仓库 Git 清理（排除测试产物）
7. ✅ 服务日志多记录Bug修复
8. ✅ AI建议测试断言修复
9. ✅ photo_url DDL 持久化（varchar(500) → mediumtext）
10. ✅ ServiceType 归属隔离修复
11. ✅ 质检查 orderId 多结果 Bug 修复
12. ✅ 评价查 orderId 接口缺失修复
13. ✅ 评价统计接口 500 Bug 修复
14. ✅ 前端链路串联（质检→评价→服务日志）
15. ✅ 测试套件新增（17个测试用例）

---

## 当前状态

### 已完成的功能模块
- 服务商管理（Provider）✅
- 服务人员管理（Staff）✅ — 含照片上传、账户同步
- 老人档案管理（Elder）✅ — 含健康档案
- 订单管理（Order）✅
- 预约管理（Appointment）✅
- 服务日志（ServiceLog）✅
- 质检管理（QualityCheck）✅
- 服务评价（Evaluation）✅
- 财务结算（Settlement）✅
- 系统配置（Config/Dict）✅
- 驾驶舱（Cockpit）✅
- 动态菜单系统 ✅
- STAFF 角色数据隔离 ✅

### Git 当前状态（2026-04-25）
```
根仓库 (11-newserver-plt):  master  ✅ 已推送 eb64bd6
elderly-care-server:        master  ✅ 已推送 eb64bd6
dingfeng-work:              main    ✅ 已推送 f356518d
```

### 测试基线（2026-04-25）
```
49 passed / 4 skipped / 0 failed
├── 隔离测试              17 passed
├── 质检 API               7 passed (TC-QC-001~007)
├── 满意度评价 TDD         5 passed (T01~T04, T07)
├── 评价 API               8 passed (TC-LEC-001~008)
├── 链路 E2E               8 passed (TC-LEC-01~08)
├── 订单统计隔离           5 passed (TC-OS-01~05)
└── skipped: T05/T06 等数据就绪后解除
```

---

## 已知问题（待修复）

### P1: 测试数据不足
- 老人档案和服务商主数据为空，无法完整测试健康档案选择功能
- **建议**：补充测试数据后重新验证相关功能

### P2: photo_url DDL 未持久化
- `t_staff.photo_url` 和 `t_elder.photo_url` 在运行中容器改为 `mediumtext`，但未生成 SQL 脚本
- **位置**：应生成 `sql/photo-url-mediumtext.sql`

### P2: 前端未跟踪文件
- `dingfeng-work/src/main/java/` 下有 health advice 相关 Java 文件（后端代码？），待确认处理

### P2: 权限体系待统一
- 旧版权限（按钮级）与新版权限（菜单级）并存
- `t_user.user_type` 与 `t_role.role_code` 无绑定关系
- PROVIDER用户若无 `provider_id` → 不触发数据隔离 → 看到全部数据
- **建议**：后续统一权限体系，或废弃旧版，或完善 R002-R005 的按钮数据

### P2: 订单统计接口无 providerId 过滤
- PROVIDER 可看全局统计数据
- **建议**：Phase 2 完善统计层数据隔离

---

## 开发规范（重要）

### 前端规范
1. **PersonCard 组件复用规则**：老人档案和服务人员卡片必须使用 PersonCard 组件
2. **NUpload custom-request 模式注意事项**：不会自动弹出文件选择器，需使用原生 `<input type="file">`
3. **API 错误处理模式**：`createFlatRequest` 返回 `{ data, error }` 不抛出异常
4. **分页参数字段名映射**：各 DTO 分页字段不统一，必须查源码确认
5. **字段名一致性**：修改前后端数据对接前，必须查对方代码确认实际字段名
6. **删除操作二次确认**：所有删除按钮必须用 `NPopconfirm` 包裹
7. **格式化规范**：`formatter.ts` 提供格式化函数，返回值已含符号
8. **前端端口**：前端端口是 9527，不是 9528

### 后端规范
1. **MySQL 连接字符集**：Docker MySQL 连接必须指定字符集 `--default-character-set=utf8mb4`
2. **关联记录创建后回写外键**：先插入主表，再更新从表外键
3. **权限接口 URL 匹配**：`PermissionInterceptor` 使用 AntPathMatcher
4. **统计接口 null 语义**：统计类接口在无数据时必须返回 `null` 而非 `0`
5. **VO 字段类型选择**：评分类字段用 `Double`（可null），不用 `double`

---

## 常用命令

```bash
# 前端热更新
cd dingfeng-work && npm run dev  # 端口 9527

# 后端重启
cd elderly-care-server && mvn spring-boot:run

# 数据库
docker start mysql-dev
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4

# Playwright 测试
cd dingfeng-work && npx playwright test

# 查看后端编译后的 class（确认是否是最新的）
ls elderly-care-server/target/classes/com/elderlycare/mapper/staff/
```

---

## 真实账号 ID 速查

| 账号 | providerId（长） | 说明 |
|------|-----------------|------|
| FWS1 | `2044978647030419457` | PROVIDER_ADMIN，短ID=1 |
| FWS2 | `2044978337352372225` | PROVIDER_ADMIN，短ID=2 |
| staff001 | `3` | STAFF，providerId=3 |

---

## 下一步开发建议

1. **补充测试数据**：插入老人档案和服务商主数据，完善测试覆盖率
2. **生成 photo_url DDL SQL 脚本**：持久化字段类型变更
3. **完善权限体系**：统一旧版按钮级权限和新版菜单级权限
4. **订单统计接口数据隔离**：添加 providerId 过滤
5. **前端未跟踪文件处理**：确认 `src/main/java/` 下的 Java 文件是否应该移到后端

---

*本文档由董老师编写，供 Hermes Agent 接手项目使用*
