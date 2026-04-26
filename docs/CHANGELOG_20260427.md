# 变更说明 v2026-04-27

## 主题：预约→订单→服务日志→质检→评价 业务链路串联

### 背景
此前各环节互相割裂：订单详情不展示预约信息，日志提交审核后无法直接跳到质检，质检详情没有评价入口。本次改进让用户在任意环节都能顺畅流转到下一步。

---

## 一、后端变更

### 1. `t_service_evaluation` 表扩展
新增两个关联字段，支持评价单与质检单/服务日志双向追溯：

| 字段 | 类型 | 说明 |
|------|------|------|
| quality_check_id | VARCHAR(64) | 关联质检单ID |
| service_log_id | VARCHAR(64) | 关联服务日志ID |

### 2. ServiceEvaluation Entity/VO/DTO 扩展
- `ServiceEvaluation.java`：加 `qualityCheckId`、`serviceLogId` 字段
- `EvaluationVO.java`：加 `qualityCheckId`、`serviceLogId` 字段
- `CreateEvaluationDTO.java`：加 `qualityCheckId`、`serviceLogId` 可选字段（从质检详情跳转时带入）

### 3. 订单详情加预约信息（之前漏掉的关联）
- `OrderServiceImpl.java`：查询关联预约单，回写 `appointmentId`、`appointmentNo`、`appointmentTime` 到 `OrderDetailVO`
- `OrderDetailVO.java`：加三个预约关联字段

### 4. 质检按服务日志ID查询
- `QualityCheckService.java`：新增 `getQualityCheckByServiceLogId(String)` 方法
- `QualityCheckServiceImpl.java`：实现按 serviceLogId 精确查询（ORDER BY DESC + LIMIT 1）
- `QualityCheckController.java`：新增 `GET /api/quality-check/service-log/{serviceLogId}` 路由

---

## 二、前端变更

### 1. 订单详情页（order/index.vue）
- 时间轴"服务中"节点展示服务日志审核状态（草稿/已提交/已通过/已驳回）+ 质检结果（待质检/合格/不合格/需整改）
- 新增 `goToServiceLog()`、`goToQualityCheck()` 跳转函数
- 新增状态映射函数：`getAuditStatusLabel`、`getAuditStatusType`、`getQualityResultLabel`、`getQualityResultType`
- 引入 `NBadge` 组件突出显示状态

### 2. 服务日志页（service-log/index.vue）
- **提交审核成功弹窗**：审核通过后弹出 NModal，提示"是否立即创建质检单"
  - "立即创建" → 跳转质检页（带 `orderNo` + `serviceLogId`）
  - "暂不需要" → 关闭弹窗，刷新列表
- **onMounted 参数接收修复**：原检查 `route.query.orderId`（错误）→ 改为 `route.query.orderNo`（与跳转来源字段对齐）

### 3. 质检页（quality/index.vue）
- **接收 serviceLogId 参数**：onMounted 从 `route.query.serviceLogId` 读取并预填创建表单
- **新建表单初始化**：加 `serviceLogId: ''` 字段
- **质检执行抽屉**：新增 `inspectDrawer`（质检员给质检单打分、选结果、填备注）
- **发起评价按钮扩展**：跳转评价页时带 `qualityCheckId` + `serviceLogId`

### 4. 评价页（evaluation/index.vue）
- **接收跳转参数**：onMounted 从 `route.query.qualityCheckId` + `route.query.serviceLogId` 读取并预填表单

### 5. API 层
- `service-log.ts`：新增 `fetchDuplicateServiceLog()`（复制服务日志）
- `quality.ts`：新增 `fetchCreateQualityCheck()`、`fetchInspect()` 封装
- `evaluation.ts`：字段映射完善

### 6. TypeScript 类型
- `evaluation.d.ts`：`EvaluationForm` + `Evaluation` 接口加 `qualityCheckId` + `serviceLogId`
- `quality.d.ts`：`QualityCheckForm` 接口加 `serviceLogId`

---

## 三、业务链路示意

```
订单详情页
  ├─ [关联预约信息] appointmentId / appointmentNo / appointmentTime
  └─ [查看服务日志] → 服务日志列表（按订单号自动筛选）
                      ├─ [提交审核] → 审核通过 → NModal提示"是否创建质检"
                      │                    ├─ [立即创建] → 质检页（orderNo+serviceLogId预填）
                      │                    └─ [暂不需要] → 关闭
                      └─ [查看详情] → 日志详情页
                                         └─ [去质检/去评价] → 质检页/评价页

质检页
  ├─ 接收 serviceLogId 预填创建表单
  └─ [提交质检] → 质检详情
                     ├─ [执行质检] → inspectDrawer（质检员打分+结果）
                     └─ [发起满意度评价] → 评价页（qualityCheckId+serviceLogId+orderNo预填）

评价页
  └─ 接收跳转参数，自动预填表单
```

---

## 四、遗留优化点
- 质检列表暂不支持按 `serviceLogId` 筛选（一个订单通常只有一条质检，影响小）
- 评价列表页暂不支持按 `qualityCheckId`/`serviceLogId` 筛选
