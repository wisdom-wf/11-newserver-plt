# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Smart Home-Based Elderly Care Service Management Platform** (智慧居家养老服务管理平台) for government oversight and service provider management in the elderly care industry.

## System Architecture

### Multi-Level User Hierarchy
The system follows a 5-level administrative hierarchy:
1. **Municipal Government** (市级) - Top-level oversight and configuration
2. **District/County** (区/县级) - Regional management
3. **Street/Town** (街道/乡镇) - Local coordination
4. **Community** (社区) - Front-line service coordination
5. **Service Provider** (服务商) - Service delivery

### Key Functional Modules

| Module | Description |
|--------|-------------|
| **Service Provider Management** (服务商管理) | Provider registration, authentication, service scope management, personnel assignments |
| **Service Personnel Management** (服务人员管理) | Personnel records, credentials, service types, service areas |
| **Order Management** (订单管理) | Order lifecycle: scheduling → dispatching → service delivery → evaluation → settlement |
| **Elderly/Client Management** (老人/客户管理) | Client profiles, care levels, government subsidies, health records |
| **Financial Settlement Management** (财务结算管理) | Service pricing, subsidy calculations, payment processing, reconciliation |
| **Service Assessment Evaluation** (服务评估评价) | Service quality scoring, client feedback, dispute handling |
| **System Configuration** (系统配置) | Data dictionaries, service types, pricing policies, area management |
| **Data Statistics** (数据统计) | Service volume reports, financial summaries, performance metrics |

### Service Types

The platform supports multiple elderly care services:
- In-home care (生活照料) - bathing, feeding, personal hygiene
- Day care (日间照料) - community-based daily care
- Meal delivery (助餐服务) - food delivery to elderly
- Cleaning services (助洁服务) - home cleaning
- Laundry services (助浴服务/助洗服务) - bathing and laundry assistance
- Health monitoring (健康管理) - blood pressure monitoring, medication reminders
- Rehabilitation assistance (康复护理) - rehabilitation exercises, medical assistance
- Mental health support (精神慰藉) - companionship, psychological support
- Information consultation (信息咨询) - policy consultation, service information
- Emergency assistance (紧急救援) - emergency response services

## Data Integration Sources

The system integrates with multiple existing government systems:
- **Civil Affairs** (民政) - elderly information, government subsidies
- **Social Security** (社保) - medical insurance, pension data
- **Health/Family Planning** (卫计) - health records, medical information
- **Business/Commerce** (工商) - business registration for providers
- **Disabled Persons Federation** (残联) - disability information

## Project Status

This project is currently in the **planning phase**. The repository contains:
- Specification document (PDF): `智慧居家养老服务管理平台V20230707(3).pdf`
- UI reference materials: screenshots and design mockups
- **Complete requirements documentation** (10 documents in `docs/requirements/`):
  - Requirements overview and 8 subsystem specifications
  - Total: ~150,000 words, covering 100+ functional modules
  - 100+ data models, 50+ RESTful APIs

No source code has been implemented yet.

## Requirements Documentation

Detailed requirements specifications are available in the `docs/requirements/` directory:

- `README.md` - Document navigation and quick reference
- `01-需求规格说明书总览.md` - Overall requirements overview
- `02-服务商管理子系统需求规格说明书.md` - Service provider management
- `03-服务人员管理子系统需求规格说明书.md` - Service personnel management
- `04-订单管理子系统需求规格说明书.md` - Order management
- `05-老人客户管理子系统需求规格说明书.md` - Elderly/client management
- `06-财务结算管理子系统需求规格说明书.md` - Financial settlement management
- `07-服务评估评价子系统需求规格说明书.md` - Service assessment and evaluation
- `08-系统配置子系统需求规格说明书.md` - System configuration
- `09-数据统计分析子系统需求规格说明书.md` - Data statistics and analysis

Refer to these documents for detailed business logic, functional requirements, and data models.

## Development Notes

### Order Lifecycle Flow
```
Client Request → Service Provider Acceptance → Personnel Assignment →
Service Delivery → Service Evaluation → Order Settlement → Payment Processing
```

### Permission Model
- Each level can only view and manage data within their jurisdiction
- Municipal level has full oversight and configuration rights
- Community and provider levels have operational rights limited to their assigned clients and personnel

### Service Pricing Structure
Services support:
- Government subsidy rates (government-funded)
- Self-pay rates (client-funded)
- Mixed pricing (subsidy + self-pay)

## Documentation

Refer to the specification PDF for detailed business logic, functional requirements, and UI/UX specifications.

## 前端开发通行规则（经验教训）

### 1. API 错误处理模式

**createFlatRequest 返回结构**：
```typescript
// ✅ 正确方式：检查返回的 error 属性
const { data, error } = await fetchSomeApi(params);
if (error) {
  message.error(error?.message || '操作失败');
  return;
}
// 成功逻辑

// ❌ 错误方式：try/catch 不适用于 createFlatRequest
try {
  await fetchSomeApi(params);
} catch (e) {
  // 这里不会捕获到错误！
}
```

**原因**: `createFlatRequest` 返回 `{ data, error }` 而不抛出异常，错误信息在 `error` 属性中。

### 2. 后端 API 字段验证

在调用后端 API 前，必须验证：
- **字段名**: 确认后端实际返回的字段名（如 `message` vs `msg`）
- **字段类型**: 确认枚举值是字符串还是数字
- **参数名**: 确认分页参数是 `page` 还是 `pageNum`

**验证方法**：
1. 查看后端 DTO/Entity 定义
2. 查看后端 Mapper XML 中的 SQL 字段映射
3. 查看后端 Service 中的状态枚举（如 `ON_JOB`, `ENABLED`）

### 3. 常见状态值对照

| 实体 | 字段 | 可用值 | 说明 |
|------|------|--------|------|
| 服务人员(Staff) | status | `PENDING`(待审核), `ON_JOB`(在职), `OFF_JOB`(离职) | 字符串，不是数字 |
| 服务商(Provider) | status | `ENABLED`(启用), `DISABLED`(禁用) | 字符串 |
| 订单(Order) | status | `CREATED`, `DISPATCHED`, `RECEIVED` 等 | 参见 OrderStatus 枚举 |

### 4. 业务校验时机

- **前端过滤**: 只展示合法的选项（如只显示在职人员、只显示启用的服务商）
- **后端校验**: 提交时后端再次校验，作为最终保障
- **错误展示**: 无论前端过滤与否，后端校验失败时必须正确显示错误信息

### 5. 代码修改检查清单

修改 API 调用或 DTO 前：
- [ ] 确认后端 Controller/Service 实际使用的参数名
- [ ] 确认后端返回的字段名（特别关注 `message` vs `msg`）
- [ ] 确认枚举值是字符串还是数字
- [ ] 测试 API 调用，验证实际返回结构
- [ ] 检查 `createFlatRequest` 调用是否正确处理 `error` 属性

### 6. useNaivePaginatedTable 分页参数映射规则

**核心原则：`params.page` 是框架内部的页码字段，前端传给后端的字段名必须与后端 DTO 一致。**

后端各 DTO 的分页字段名不统一，有的用 `current`，有的用 `page`：

| 后端 DTO | 分页字段名 | 对应前端页面 |
|----------|-----------|-------------|
| `AppointmentQueryDTO` | `current` | 预约管理 |
| `ElderPageDTO` | `current` | 老人档案 |
| `QualityCheckQueryDTO` | `current` | 质检管理 |
| `ServiceLogQueryDTO` | `current` | 服务日志 |
| `OrderQueryDTO` | `page` | 订单管理 |
| `ProviderQueryDTO` | `page` | 服务商管理 |
| `StaffQueryDTO` | `page` | 服务人员管理 |
| `EvaluationQueryDTO` | `page` | 评价管理 |
| `SettlementQueryDTO` | `page` | 财务结算 |

**正确写法**：
```typescript
// ❌ 错误：假设所有后端都用 page
const queryParams: any = { page: params.page, pageSize: params.pageSize };

// ✅ 正确：根据后端 DTO 字段名映射
// 后端用 current 的：
const queryParams: any = { current: params.page, pageSize: params.pageSize };
// 后端用 page 的：
const queryParams: any = { page: params.page, pageSize: params.pageSize };
```

**查询命令**：
```bash
grep -rn "private Integer \(current\|page\)" elderly-care-server/src/main/java/com/elderlycare/dto/
```

### 7. 关联记录创建后回写外键

创建子记录时，必须将子记录 ID 回写到父记录。例如：
```java
// ✅ 正确：创建订单后回写到预约
orderMapper.insert(order);
appointment.setOrderId(order.getOrderId());
appointment.setOrderNo(order.getOrderNo());
appointmentMapper.updateById(appointment);

// ❌ 错误：只创建不关联
orderMapper.insert(order);
// appointment 的 orderId 永远是 null
```

### 8. 修改前后端参数对接的流程

**修改任何前后端数据对接前，必须按此顺序操作**：
1. 先查后端 DTO/Controller 确认字段名（`grep` 或读源码）
2. 再改前端传参字段名
3. 浏览器 Network 面板验证请求参数是否正确
4. 确认后端返回数据是否符合预期

**禁止**：不看后端代码就假设字段名一致性，盲目统一参数名。

### 9. 权限模块开发规范

#### 新增 API 权限流程
1. **后端**：实现 Controller，URL 格式 `/api/module/resource`
2. **数据库**：在 `t_permission` 插入按钮权限记录
   ```sql
   INSERT INTO t_permission (permission_id, permission_code, permission_name,
     permission_type, parent_id, permission_url, permission_method, sort_order,
     icon, status, create_time, deleted)
   VALUES ('Pxxxx', 'module:resource:action', '操作名称', 'BUTTON',
     'Pxxxxx', '/api/module/*/action', 'PUT', 1, NULL, 'NORMAL', NOW(), 0);
   ```
3. **角色分配**：在 `t_role_permission` 分配给对应角色
4. **前端**：使用 `hasAuth('module:resource:action')` 保护操作按钮

#### 新增数据权限流程（providerId 自动注入）
1. **确认**：接口是否需要按 providerId 隔离数据
2. **注入**：在 Controller 方法中添加：
   ```java
   String autoPid = UserContext.getProviderId();
   if (autoPid != null) {
       queryDTO.setProviderId(autoPid);
   }
   ```
3. **Mapper**：确保 XML 中有 `AND provider_id = #{providerId}` 条件

#### 权限 URL 匹配规则
- PermissionInterceptor 使用 AntPathMatcher
- `*` 匹配单个路径段：`/api/orders/*/cancel`
- `**` 匹配剩余路径：`/api/users/**`

#### PermissionInterceptor 排除路径（WebMvcConfig）
登录相关接口无需认证，已在 excludePathPatterns 中配置：
- `/api/auth/login`
- `/api/auth/logout`
- `/api/auth/userinfo`

新增无需权限的接口时，需加入该列表。

#### 用户类型与数据可见范围
| userType | 说明 | 数据范围 |
|----------|------|---------|
| `SYSTEM` | 系统管理员 | 全部数据 |
| `PROVIDER` | 服务商管理员 | 自己 providerId 下的数据 |
| `STAFF` | 服务人员 | 自己 providerId 下的数据（受限于角色权限） |

#### 用户状态值
`t_user.status` 必须是 `NORMAL` 才能登录（不是 `ACTIVE`）。

### 10. 字符编码规范（重要！）

**根因**：MySQL 默认 `character_set_client = latin1`，手动 SQL 操作会导致中文乱码。

#### MySQL CLI 连接规范
```bash
# ✅ 正确：必须指定字符集
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4

# ❌ 错误：不指定字符集会导致乱码
docker exec mysql-dev mysql -uroot -proot elderly_care
```

#### SQL 文件规范
所有 SQL 种子文件开头必须包含：
```sql
USE elderly_care;
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
```

#### COLLATION 标准
| 用途 | 推荐 | 禁止 |
|------|------|------|
| 默认 | `utf8mb4_0900_ai_ci` | `utf8mb4_general_ci` |

当前已统一所有表为 `utf8mb4_0900_ai_ci`。

#### JDBC 配置
当前配置已正确，勿修改：
```properties
url: jdbc:mysql://...?characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci
```

#### 验证中文显示
```bash
# 验证数据库中文是否正常
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4 \
  -e "SELECT dict_type_id, dict_type_name FROM t_dict_type LIMIT 3;"
# 应显示正确的中文
```

### 11. 前后端字段对接规范（复盘经验）

#### 字段名一致性规则
1. **修改前必查对方代码**: 任何字段修改前，必须 `grep` 确认对方代码中的实际字段名
2. **禁止假设一致性**: 不能假设前后端字段名一致，必须逐一验证
3. **API 参数类型严格对应**: 前端 form 提交字段名必须与后端 DTO 字段名完全一致

#### 常见字段名不一致案例
| 问题场景 | 前端错误 | 后端正确 | 教训 |
|----------|----------|----------|------|
| 自付金额字段 | `selfPayFee` | `selfPayAmount` | 改前必查对方代码 |
| ID字段泛化 | `id` | `serviceLogId` | RESTful ID必须具体化 |
| 分页字段 | `page` | `current` | 需查具体DTO定义 |

#### 数组字段边界处理
前端赋值时注意 `|| []` 对字符串不会触发 fallback：
```typescript
// ❌ 错误：当 row.servicePhotos 是非空字符串时
servicePhotos: row.servicePhotos || []

// ✅ 正确：显式检查数组类型
servicePhotos: Array.isArray(row.servicePhotos)
  ? row.servicePhotos
  : row.servicePhotos ? [row.servicePhotos] : []
```

#### DTO 设计规范
1. **避免重复字段**: 不要像 `CompleteServiceDTO` 那样有 `actualFee` 和 `actualServiceFee` 重复字段
2. **列表和详情一致**: 列表 API 也应返回完整业务字段（如 `actualPrice`）

#### 测试验证规则
每次涉及字段映射的修改后：
1. 浏览器 Network 面板确认请求参数正确
2. 确认返回数据字段名和类型符合预期
3. 边界条件测试: null、0、空字符串的处理

### 12. Modal → NDrawer 改造规范

已完成的改造：elder、staff、service-log、quality、evaluation 模块的详情/编辑已统一为 NDrawer。

#### 改造步骤（以单个 Modal → Drawer 为例）

**1. 导入组件**
```typescript
import { NDrawer, NDrawerContent } from 'naive-ui';
```

**2. 状态变量命名规范**
```typescript
// ❌ 旧命名
const detailVisible = ref(false);
const modalLoading = ref(false);
const isEdit = ref(false);

// ✅ 新命名
const detailDrawerVisible = ref(false);
const drawerLoading = ref(false);
const operateType = ref<'add' | 'edit'>('add');
```

**3. 变量替换规则**
使用 `replace_all` 批量替换：
- `modalVisible.value` → `drawerVisible.value`
- `detailVisible.value` → `detailDrawerVisible.value`
- `rectifyVisible.value` → `rectifyDrawerVisible.value`
- `isEdit.value` (三元判断) → `operateType.value === 'edit'`

**4. NModal → NDrawer 模板替换**
```vue
<!-- ❌ 旧写法 -->
<NModal v-model:show="modalVisible" preset="card" style="width: 700px">
  <template #header>...</template>
  <div>...</div>
  <template #footer>...</template>
</NModal>

<!-- ✅ 新写法 -->
<NDrawer v-model:show="drawerVisible" :width="560" placement="right" closable>
  <NDrawerContent :title="operateType === 'add' ? '新增' : '编辑'" closable>
    <div>...</div>
    <template #footer>...</template>
  </NDrawerContent>
</NDrawer>
```

**5. 提交按钮 loading 状态**
```typescript
// ❌ 旧
<NButton :loading="modalLoading">

// ✅ 新
<NButton :loading="drawerLoading">
```

#### Drawer 宽度规范
| 用途 | 宽度 |
|------|------|
| 简单表单 | 480px |
| 标准详情/编辑 | 560px |
| 复杂详情（含图片） | 600px |

#### 混合模式原则（Plan C）
- **Drawer**: 详情查看、复杂表单编辑、多步骤操作
- **Modal**: 图片预览、快速确认、提示信息
- **不强制统一**: 根据操作复杂度选择合适容器

### 14. PersonCard组件复用规范（经验教训）

**问题**: staff/index.vue 手写了卡片代码而不是复用PersonCard组件，导致：
- photoUrl上传按钮点击无反应（控制台无错误）
- 代码重复，维护成本高

**正确做法**: 老人健康档案和服务人员卡片都应使用 `PersonCard` 组件：
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

**PersonCard的NUpload机制**:
- `custom-request` 返回 `false` 阻止默认上传
- 手动 emit `photo-upload` 事件传递 File 对象
- 父组件在事件处理中读取File并转为Base64

**验证**: 强制刷新浏览器(Cmd+Shift+R)清除缓存

### 15. 删除操作二次确认规范

**所有删除操作必须使用 `NPopconfirm` 组件进行二次确认**，防止误删数据。

#### 标准写法
```vue
<!-- ✅ 正确：使用 NPopconfirm 包裹删除按钮 -->
<n-popconfirm @positive-click="handleDelete(row)">
  <template #trigger>
    <n-button type="error" size="small">删除</n-button>
  </template>
  确定要删除该记录吗？
</n-popconfirm>

<!-- ❌ 错误：直接使用按钮，无确认 -->
<n-button type="error" size="small" @click="handleDelete(row)">删除</n-button>
```

#### 需要确认的删除场景
| 场景 | 确认内容 |
|------|----------|
| 列表删除按钮 | "确定要删除该记录吗？此操作不可恢复。" |
| 批量删除 | "确定要删除选中的 {n} 条记录吗？此操作不可恢复。" |
| 级联删除 | "删除将同时移除关联数据，确定继续吗？" |

#### 实现示例
```vue
<template>
  <n-popconfirm @positive-click="handleDelete(row)">
    <template #trigger>
      <n-button type="error" size="small">
        <icon:delete /> 删除
      </n-button>
    </template>
    确定要删除该记录吗？此操作不可恢复。
  </n-popconfirm>
</template>

<script setup>
const handleDelete = async (row) => {
  try {
    await deleteApi(row.id);
    message.success('删除成功');
    refreshTable();
  } catch (error) {
    message.error('删除失败');
  }
};
</script>
```

#### 已完成改造的模块
- elder (老人档案)
- staff (服务人员)
- service-log (服务日志)
- quality (质检管理)
- evaluation (评价管理)
- order (订单管理)
- appointment (预约管理)

#### 按钮文字规范
- 删除按钮：使用 `type="error"` 样式，图标 + 文字
- 确认文字：简洁明确，说明操作后果

### 16. 修改实体前必须验证数据库结构（重要！）

**问题**：修改 `ProviderServiceType` 实体时，每次只修一个字段，修改了 3 次才成功。

**根因**：没有先查数据库实际结构就修改，导致"头痛医头脚痛医脚"。

**正确流程**：

1. **修改前先查数据库结构**
```bash
docker exec mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4 -e "DESCRIBE t_table_name;"
```

2. **对比实体字段与数据库列**
   - 实体有但数据库没有的字段 → 移除或加 `@TableField(exist = false)`
   - 数据库有但实体没有的字段 → 添加到实体
   - 字段名不一致 → 改成数据库实际列名

3. **修改后必须用 curl 自测**
```bash
# 获取 Token
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 测试 API
curl -s "http://localhost:8080/api/providers/{id}" -H "Authorization: Bearer $TOKEN"
```

4. **验证通过后再让用户测试** — 不能把未经验证的修改直接交给用户测

**核心原则**：数据库是唯一真相，代码中的实体定义可能过时，必须以数据库为准。

