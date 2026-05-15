# 变更日志 2026-05-16

## 后端改动 (elderly-care-server)

### Bug 修复

**Bug 1: 派单时合同创建失败导致页面空转**
- 文件: `src/main/java/com/elderlycare/service/order/impl/OrderServiceImpl.java`
- 修复: 派单异常时打印 warn 日志告知合同状态，`dispatchVO.setContractCreated(false)` 通知前端
- 场景: ESS 合同服务不可用时，前端不再空转，给出明确提示

**Bug 2: 满意度调查提交后 images 字段丢失**
- 文件: `src/main/java/com/elderlycare/entity/evaluation/ServiceEvaluation.java`
- 修复: 新增 `images` 字段 (`evaluation_images` VARCHAR(2000) 列)，`getImages()`/`setImages()` 正确读写

**Bug 3: 健康观察字段空值阻断提交流程**
- 文件: `src/main/java/com/elderlycare/service/servicelog/impl/ServiceLogServiceImpl.java`
- 修复: `healthObservations` 改为 warn 提示，不阻断提交（健康观察为选填字段）

**Bug 4: 质检整改分支 NPE 导致服务日志丢失**
- 文件: `src/main/java/com/elderlycare/service/quality/impl/QualityCheckServiceImpl.java`
- 修复: `NEED_RECTIFY` 分支加 null 检查 + 默认文案，避免 NPE 和脏数据

**Bug 5: avgScore 除零导致服务崩溃**
- 文件: `src/main/java/com/elderlycare/service/evaluation/impl/ServiceEvaluationServiceImpl.java`
- 修复: `submitSurveyByToken` 内 avgScore 计算逐项 null 检查，count=0 返回 0

**Bug 6: 实际服务人员与派单人不同时 staffId 丢失**
- 文件: `src/main/java/com/elderlycare/service/order/impl/OrderServiceImpl.java`
- 修复: `completeService` 末尾加 `order.setStaffId(dto.getStaffId())`，支持临时换人

**Bug 7: avgScore 计算错误包含 environmentScore**
- 文件: `src/main/java/com/elderlycare/service/evaluation/impl/ServiceEvaluationServiceImpl.java`
- 确认: avgScore = (serviceScore+attitudeScore+skillScore+punctualityScore)/4，不含 environmentScore ✅

### 业务功能改动

**P0-4: 订单完成后自动生成满意度调查 token**
- 文件: `ServiceEvaluationServiceImpl.java`, `QualityCheckServiceImpl.java`
- 质检 `inspect()` 合格后 → `generateToken()` 生成评价 token → `Order` 联动更新为 EVALUATED 状态

**P0-5: 健康档案数据写入**
- 文件: `ServiceLogServiceImpl.java`
- `submitForReview()` 时写入 `t_health_measurement`（类型=HEALTH_OBSERVATION）

**新增字段**
- `DispatchVO.java`: 新增 `contractCreated` 字段，通知前端合同是否创建成功
- `CreateOrderDTO.java`: 确认 `orderSource` 字段存在

### 数据库变更
- `t_service_evaluation`: 新增 `evaluation_images` VARCHAR(2000) 列

---

## 前端改动 (dingfeng-work)

**订单卡片布局紧凑化**
- 文件: `src/views/business/order/index.vue`
- 改动:
  - 筛选区: 去掉灰色盒包裹，紧凑单行布局（高度 ~50px）
  - 卡片: 单列 → 3列 grid 布局（高度 ~90px，原来 ~110px）
  - 按钮: `size="small"` → `size="tiny"`
  - 分页: `pageSizes` 从 `[10, 20, 50]` 改为 `[12, 24, 48]`
- 效果: 每屏订单数从约 6 条提升到约 15 条

---

## 生产环境部署

- JAR: `elderly-care-server-1.0.0.jar` (buildTime: 2026-05-15 18:32) ✅ 已部署
- 前端: `dist/` (buildTime: 2026-05-16 00:07) ✅ 已部署
- DB: `t_service_evaluation.evaluation_images` 列已添加

## 待完成 (Pending)

- [ ] `t_service_evaluation` 剩余 7 个列: environment_score, professional_score, tags, token_status, token_expire_time, token_used_time, token_used_ip
- [ ] service-log 查询超时优化（表有 MEDIUMTEXT 大字段，建议加索引）
