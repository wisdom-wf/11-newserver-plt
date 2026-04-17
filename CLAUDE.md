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
