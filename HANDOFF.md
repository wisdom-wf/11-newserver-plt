# 项目交接文档 — 智慧居家养老服务管理平台

> 本文档由董老师编写，供 Claude Code 接手继续开发使用。
> 最后更新：2026-04-21

---

## 一、项目概览

### 基本信息
- **项目名称**：智慧居家养老服务管理平台
- **仓库**：https://github.com/wisdom-wf/11-newserver-plt
- **技术栈**：Spring Boot + Vue3 (NaiveUI) + MySQL + Docker
- **端口**：前端 9528，后端 8080

### 仓库结构
```
11-newserver-plt/               # Git根仓库（子模块结构）
├── elderly-care-server/        # Spring Boot 后端（主仓库）
│   └── src/main/java/com/elderlycare/
│       ├── controller/        # REST API
│       ├── service/            # 业务逻辑
│       ├── mapper/             # MyBatis Mapper
│       ├── entity/             # 数据库实体
│       ├── dto/                # 数据传输对象
│       └── vo/                 # 视图对象
├── dingfeng-work/              # Vue3 前端（Git子模块，GitHub: elderly-care-web）
│   └── src/
│       ├── views/business/     # 业务页面
│       ├── components/common/  # 通用组件
│       └── service/api/        # API调用层
└── sql/                        # SQL脚本
```

### 快速启动
```bash
# 后端
cd elderly-care-server && mvn spring-boot:run

# 前端
cd dingfeng-work && npm run dev

# 数据库（Docker）
docker start mysql-dev
```

---

## 二、最近完成的工作（2026-04-20 ~ 2026-04-21）

### 1. 服务人员照片上传功能 ✓

**问题描述**：服务人员卡片点击上传按钮无反应，老人档案页面上传正常。

**根因分析**：
1. staff/index.vue 手写了卡片代码而不是复用 PersonCard 组件，导致 photo upload 逻辑不一致
2. PersonCard 组件使用 NUpload `custom-request` 模式，但 NUpload 在该模式下**不会自动弹出文件选择器**
3. `t_staff.photo_url` 字段类型为 `varchar(500)`，base64 DataURL 超过长度限制

**修复方案**：
1. staff/index.vue：移除 ~70 行手写卡片代码，改用 `<PersonCard>` 组件
2. person-card.vue：用原生 `<input type="file" hidden>` + `ref` + `triggerUpload()` 替代 NUpload
3. 数据库：`ALTER TABLE t_staff MODIFY COLUMN photo_url MEDIUMTEXT;`
4. 后端：新增 `PUT /api/staff/{staffId}/photo` 接口 + `avatarUrl` 字段

**修改文件**：
| 文件 | 改动 |
|------|------|
| `person-card.vue` | 新增 `ref` import、`fileInputRef`、`triggerUpload`、`handleFileChange` |
| `staff/index.vue` | 手写卡片 → `<PersonCard>` 组件 |
| `staff.ts` | 新增 `updateStaffPhoto` API |
| `StaffController.java` | 新增 `/api/staff/{staffId}/photo` PUT 接口 |
| `StaffService.java` | 新增 `updatePhoto` 方法 |
| `StaffVO.java` | 新增 `avatarUrl` 字段 |
| `StaffCreateDTO.java` | 新增 `avatarUrl` 字段 |
| MySQL t_staff 表 | `photo_url` varchar(500) → mediumtext |

**Commit**：
- frontend: `42e3c8eb` feat(staff): 照片上传功能修复 + PersonCard组件复用
- backend: `dcf5e00` feat(staff): 新增服务人员照片相关接口与字段

### 2. 已完成的 Playwright E2E 测试

测试文件位于 `dingfeng-work/e2e/tests/`：
- `elder-photo.spec.ts` — 8个测试，老人照片上传流程，**全部通过**
- `servicelog-api.spec.ts` — 10个测试，服务日志API，3个失败（后端500错误，非前端问题）
- `staff-service-log-link.spec.ts` — 9个测试
- `health-archive/health-index.spec.ts` — 7个测试
- `staff-service-log-ui.spec.ts` — 8个测试
- `ui-quality-audit.spec.ts` — UI质量审计
- `ue-interaction-test.spec.ts` — UE交互测试

---

## 三、当前状态

### 已完成的功能模块
- 服务商管理（Provider）
- 服务人员管理（Staff）— 照片功能刚修复
- 老人档案管理（Elder）— 含健康档案
- 订单管理（Order）
- 预约管理（Appointment）
- 服务日志（ServiceLog）
- 质检管理（QualityCheck）
- 服务评价（Evaluation）
- 财务结算（Settlement）
- 系统配置（Config/Dict）
- 驾驶舱（Cockpit）— 公开页 + 认证页

### 待完成的功能（来自需求文档）
- **R005 STAFF 角色菜单权限配置** — `t_role_menu` 表未给 STAFF 角色分配菜单
- **数据隔离** — ServiceLog、Order 等接口需按 `staffId` 过滤（STAFF 角色只能看自己的数据）
- **登录接口返回 staffId** — 当前登录返回 `userId`，服务人员需知自己的 `staffId`

---

## 四、已知问题

### 1. 服务日志 API 3个测试失败
**文件**：`servicelog-api.spec.ts` TC-SL-API-003/004/008
**原因**：后端对不存在的 orderId 返回 500 而非业务错误数据
**状态**：非阻塞，前端功能正常

### 2. Staff 角色的数据隔离未实现
STAFF 用户登录后，订单/服务日志等接口没有按 `staffId` 过滤数据。
所有 STAFF 用户可以看到所有订单。
**修复位置**：
- `ServiceLogController`、`OrderController` 等需检查 `UserContext.getStaffId()` 并注入查询条件

### 3. t_staff.photo_url 字段修改未持久化
字段类型修改（varchar → mediumtext）是在运行中的 Docker 容器执行的，未生成 SQL 脚本。
**建议**：在 `sql/` 目录生成 `schema-photo-fix.sql` 持久化该变更。

---

## 五、开发规范（重要）

### 前端规范

#### 1. PersonCard 组件复用规则
老人档案和服务人员卡片**必须使用 PersonCard 组件**，禁止手写卡片 div。
```vue
<PersonCard
  :photo-url="staff.avatarUrl"
  :name="staff.staffName || '未知'"
  :subtitle="staff.phone || '-'"
  :extra-info="[{ label: '状态', value: getStatusLabel(staff.status) }]"
  :index-value="staff.rating"
  index-label="评分"
  :show-upload-btn="true"
  @photo-upload="(file) => handlePhotoUpload(staff.staffId, file)"
/>
```

#### 2. NUpload custom-request 模式注意事项
NaiveUI NUpload 在 `custom-request` 模式下**不会自动弹出文件选择器**。
正确做法：使用原生 `<input type="file" ref="xxxRef" hidden>` + `@click.stop` 触发 `input.click()`。
```typescript
import { ref } from 'vue';
const fileInputRef = ref<HTMLInputElement | null>(null);
function triggerUpload() { fileInputRef.value?.click(); }
function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (file) emit('photo-upload', file);
  (e.target as HTMLInputElement).value = ''; // 重置
}
```

#### 3. API 错误处理模式
`createFlatRequest` 返回 `{ data, error }` **不抛出异常**，错误在 `error` 属性中：
```typescript
const { data, error } = await fetchSomeApi(params);
if (error) { message.error(error?.message || '操作失败'); return; }
// 成功逻辑
```

#### 4. 分页参数字段名映射
后端各 DTO 的分页字段不统一，**必须查源码确认**：
```bash
grep -rn "private Integer \(current\|page\)" elderly-care-server/src/main/java/com/elderlycare/dto/
```
| 后端DTO | 字段名 |
|---------|--------|
| OrderQueryDTO | page |
| StaffQueryDTO | page |
| ElderPageDTO | current |
| ServiceLogQueryDTO | current |
| AppointmentQueryDTO | current |

#### 5. 字段名一致性
修改前后端数据对接前，**必须查对方代码**确认实际字段名：
```bash
grep -rn "selfPay\|avatarUrl\|photoUrl" elderly-care-server/src/main/java/
```

#### 6. 删除操作二次确认
所有删除按钮必须用 `NPopconfirm` 包裹：
```vue
<n-popconfirm @positive-click="handleDelete(row)">
  <template #trigger><n-button type="error">删除</n-button></template>
  确定要删除该记录吗？
</n-popconfirm>
```

#### 7. 格式化规范
`formatter.ts` 提供：`formatMoney`/`formatPercent`/`formatScore`/`formatCount`/`formatNumber`
- 返回值**已含符号**（¥/%），模板中不要再加：`{{ formatMoney(x) }}` 而非 `¥{{ formatMoney(x) }}`
- 业务0值显示 `"--"` 而非 "0"

### 后端规范

#### 1. MySQL 连接字符集
Docker MySQL 连接**必须指定字符集**：
```bash
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4
```
不指定会导致中文乱码。

#### 2. 关联记录创建后回写外键
```java
orderMapper.insert(order);
appointment.setOrderId(order.getOrderId());
appointmentMapper.updateById(appointment);
```

#### 3. 权限接口 URL 匹配
`PermissionInterceptor` 使用 AntPathMatcher：
- `*` 匹配单段：`/api/orders/*/cancel`
- `**` 匹配多段：`/api/users/**`

#### 4. STAFF 角色数据隔离注入点
在需要数据隔离的 Controller 方法中添加：
```java
String staffId = UserContext.getStaffId();
if (staffId != null) {
    queryDTO.setStaffId(staffId);
}
```
Mapper XML 中需有对应条件：`AND staff_id = #{staffId}`

---

## 六、Git 工作流

### 提交规范
```
feat(module): 简短描述
feat(module): 另一条描述

1. 具体改动1
2. 具体改动2
3. 具体改动3
```

### 当前仓库状态
- **根仓库 (11-newserver-plt)**：master 分支，已推送
- **子模块 (dingfeng-work)**：main 分支，已推送
- **GitHub Token**：已配置（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）

### 子模块更新后父仓库需同步
```bash
cd 11-newserver-plt
git add dingfeng-work && git commit -m "chore: 更新子模块" && git push
```

---

## 七、数据库

### 连接方式
```bash
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4
```

### 关键表结构差异（教训）
| 表 | photo_url 类型 | 说明 |
|----|---------------|------|
| t_elder | mediumtext | OK，base64 DataURL 够存 |
| t_staff | mediumtext | 已修复（原为 varchar(500)，导致上传失败） |

---

## 八、下一步建议

### 高优先级
1. **R005 STAFF 角色菜单权限** — 在 `t_role_menu` 配置，否则服务人员登录后无菜单
2. **数据隔离** — ServiceLog/Order 接口加 staffId 过滤
3. **登录返回 staffId** — 服务人员知道自己是谁

### 中优先级
4. **持久化 photo_url DDL** — 生成 `sql/photo-url-mediumtext.sql`
5. **服务日志 API 测试修复** — 后端对无效 orderId 的错误处理
6. **Playwright 测试完善** — staff 照片上传自动化测试

### 低优先级
7. **健康档案详情页** — 目前只有卡片列表，缺详情编辑页
8. **驾驶舱图表优化** — 公开页图表刷新逻辑

---

## 九、常用命令

```bash
# 前端热更新
cd dingfeng-work && npm run dev  # 端口 9528

# 后端重启
cd elderly-care-server && mvn spring-boot:run

# 数据库
docker start mysql-dev
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4 -e "SQL"

# Playwright 测试
cd dingfeng-work && npx playwright test

# 查看后端编译后的 class（确认是否是最新的）
ls elderly-care-server/target/classes/com/elderlycare/mapper/staff/
```

---

## 十、联系信息

- **凡哥（王凡）**：陕西红泥数智科技创始人，AI+政务数字化专家
- **项目定位**：智慧居家养老服务管理平台（政府监管 + 服务商管理）
- **核心用户**：市级政府、街道社区、服务商、服务人员、老人

---

*本文档由董老师（Hermes Agent）编写，如有疑问请翻阅 CLAUDE.md 或项目需求文档。*
