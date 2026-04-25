# 服务日志/质检/评价模块工作流程重构 - 变更说明

**日期**: 2026-04-25
**提交**: `84c800b`
**分支**: master

---

## 一、问题背景

用户反馈原三个模块（服务日志、质检、满意度评价）UI/UE 设计体现不出业务关联关系，且存在残留数据影响。

### 原业务流程（错误）
```
订单开始服务 → 自动创建质检单 → 服务日志提交审核 → ...
```

### 正确业务流程
```
订单开始服务 → 服务日志(草稿) → 提交审核 → 自动创建质检请求 → 质检审核 → 完成服务 → 可选发起评价
```

---

## 六、订单时间轴优化（新增）

**提交**: `84c800b`

在订单详情页面的时间轴中，"服务中"节点增加"查看服务日志"快速链接。

### 改动文件
- `dingfeng-work/src/views/business/order/index.vue`

### 功能说明
- 管理员/质检员可在订单时间轴中直接查看该订单的服务日志
- 点击链接跳转到服务日志页面，自动带上订单号筛选条件

---

## 二、核心改动

### 2.1 后端改动

#### ServiceLogServiceImpl.java
**文件**: `elderly-care-server/src/main/java/com/elderlycare/service/servicelog/impl/ServiceLogServiceImpl.java`

**提交审核方法 `submitForReview()`** 新增自动创建质检单逻辑：

```java
// 自动创建质检请求
QualityCheck qualityCheck = new QualityCheck();
qualityCheck.setQualityCheckId(IDGenerator.generateId());
qualityCheck.setCheckNo("QC" + System.currentTimeMillis());
qualityCheck.setOrderId(serviceLog.getOrderId());
qualityCheck.setOrderNo(serviceLog.getOrderNo());
qualityCheck.setServiceLogId(serviceLog.getServiceLogId());
qualityCheck.setServiceCategory(serviceLog.getServiceTypeCode());
qualityCheck.setProviderId(serviceLog.getProviderId());
qualityCheck.setStaffId(serviceLog.getStaffId());
qualityCheck.setCheckType("COMPLETION");
qualityCheck.setCheckMethod("PHOTO_REVIEW");
qualityCheck.setCheckResult("PENDING");
qualityCheck.setRectifyStatus("PENDING");
qualityCheck.setCreateTime(LocalDateTime.now());
qualityCheckMapper.insert(qualityCheck);
```

#### OrderServiceImpl.java
**文件**: `elderly-care-server/src/main/java/com/elderlycare/service/order/impl/OrderServiceImpl.java`

**开始服务方法 `startService()`** 移除了自动创建质检单的逻辑（原错误做法）。

### 2.2 前端改动

#### 新增 FlowIndicator.vue 组件
**文件**: `dingfeng-work/src/components/business/FlowIndicator.vue`

流程指示器组件，显示：
- 已完成步骤：绿色勾选 ✓
- 当前步骤：白色高亮 + 数字
- 未完成步骤：灰色圆圈 ○

**步骤配置**：
```typescript
const flowSteps = [
  { key: 'service_started', label: '服务开始' },
  { key: 'log_submitted', label: '日志待审核' },
  { key: 'log_approved', label: '审核通过' },
  { key: 'service_completed', label: '服务完成' },
  { key: 'evaluated', label: '已完成评价' }
];
```

#### 服务日志页面 (service-log/index.vue)

| 日志状态 | 显示按钮 |
|---------|---------|
| DRAFT | [编辑] [删除] [提交审核] |
| SUBMITTED | [详情] [审核]（质检角色可见）|
| APPROVED | [详情] [去完成服务] |
| COMPLETED | [详情] [去评价] |

**注意**：移除了 APPROVED 状态下错误的"去质检"按钮（质检在提交审核时已自动创建）。

#### 质检页面 (quality/index.vue)

| 质检状态 | 显示按钮 |
|---------|---------|
| PENDING | [详情] |
| QUALIFIED | [详情] [去完成服务] |
| UNQUALIFIED | [详情] [整改] |

#### 评价页面 (evaluation/index.vue)

| 订单状态 | 显示内容 |
|---------|---------|
| 待评价 | 流程指示器 + 创建评价入口 |
| 已评价 | 流程指示器 + 评价详情 |

---

## 三、数据验证

### 3.1 测试数据场景

| 订单号 | 订单状态 | 日志状态 | 质检状态 | 场景 |
|--------|----------|----------|----------|------|
| ORD202604176461 | SERVICE_STARTED | SUBMITTED | PENDING | 待审核 |
| ORD202604173647 | SERVICE_STARTED | SUBMITTED | UNQUALIFIED | 质检不合格 |
| ORD202604171297 | SERVICE_STARTED | REJECTED | UNQUALIFIED | 审核驳回 |
| ORD202604174435 | SERVICE_STARTED | DRAFT | - | 草稿未提交 |
| ORD202604178664 | SERVICE_COMPLETED | APPROVED | QUALIFIED | 已完成（无评价）|
| ORD202604175791 | SERVICE_COMPLETED | APPROVED | QUALIFIED | 已完成（有评价）|

### 3.2 API 测试验证

1. ✅ `PUT /api/service-log/{id}/submit-review` - 提交审核时自动创建质检单
2. ✅ `PUT /api/service-log/{id}/review` - 审核服务日志（APPROVED/REJECTED）
3. ✅ `PUT /api/orders/{id}/complete` - 完成服务

---

## 四、影响范围

### 4.1 涉及文件

**后端 (elderly-care-server)**:
- `ServiceLogServiceImpl.java` - 核心逻辑修改
- `OrderServiceImpl.java` - 移除旧逻辑

**前端 (dingfeng-work)**:
- `FlowIndicator.vue` - 新增组件
- `service-log/index.vue` - 流程指示器 + 按钮逻辑
- `quality/index.vue` - 流程指示器 + 按钮逻辑
- `evaluation/index.vue` - 流程指示器

### 4.2 数据库变更

无数据库结构变更，仅涉及数据清理：
- 清理残留测试数据（服务日志、质检单关联关系）

---

## 五、注意事项

1. **质检单创建时机**：必须在服务日志提交审核时创建，而非订单开始服务时
2. **按钮权限**：审核按钮仅质检角色可见，使用 `hasAuth('service-log:list:review')` 控制
3. **流程引导**：审核通过后引导用户"去完成服务"而非"去质检"

---

## 六、后续优化建议

1. 完成服务后自动触发评价通知
2. 质检不合格时自动通知服务人员整改
3. 统计各步骤平均耗时
